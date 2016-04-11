package cn.momia.mapi.api.index;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.subject.Subject;
import cn.momia.api.discuss.DiscussServiceApi;
import cn.momia.api.discuss.dto.DiscussTopic;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/v3/index")
public class IndexV3Api extends AbstractIndexApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexV3Api.class);

    private static final int HOT_COURSE = 1;
    private static final int NEW_COURSE = 2;

    private static Date lastChangeTime = new Date();
    private static int subjectCourseType = Math.random() > 0.5 ? HOT_COURSE : NEW_COURSE;

    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private DiscussServiceApi discussServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse index(HttpServletRequest request,
                                   @RequestParam(required = false, defaultValue = "") String utoken,
                                   @RequestParam(value = "city") int cityId,
                                   @RequestParam int start) {
        if (cityId < 0) return MomiaHttpResponse.FAILED("无效的CityID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        JSONObject indexJson = new JSONObject();
        if (start == 0) {
            int platform = getPlatform(request);
            String version = getVersion(request);

            indexJson.put("banners", getBanners(cityId, platform, version));

            indexJson.put("newUser", false);
            if (!StringUtils.isBlank(utoken)) {
                User user = userServiceApi.get(utoken);
                if (!user.isPayed()) {
                    indexJson.put("newUser", true);
                    indexJson.put("eventsTitle", "新用户专享");
                    indexJson.put("events", getEvents(cityId, platform, version, 2));
                } else {
                    indexJson.put("eventsTitle", "老用户专享");
                    indexJson.put("events", getEvents(cityId, platform, version, 1));
                }
            } else {
                indexJson.put("newUser", true);
                indexJson.put("eventsTitle", "新用户专享");
                indexJson.put("events", getEvents(cityId, platform, version, 2));
            }

            Date now = new Date();
            if (now.getTime() - lastChangeTime.getTime() >= 3 * 24 * 60 * 60 * 1000) { // 3天一轮换
                if (subjectCourseType == HOT_COURSE) subjectCourseType = NEW_COURSE;
                else subjectCourseType = HOT_COURSE;

                lastChangeTime = new Date();
            }

            List<Subject> subjects = subjectServiceApi.list(cityId);

            int currentSubjectCourseType = subjectCourseType;
            JSONArray subjectsJson = new JSONArray();
            for (Subject subject : subjects) {
                if (subject.getId() == 14) continue;

                List<Course> courses = courseServiceApi.listBySubject(subject.getId());
                if (courses == null || courses.isEmpty()) continue;

                completeLargeImg(subject);
                completeMiddleCoursesImgs(courses);

                currentSubjectCourseType = currentSubjectCourseType % 2 + 1;
                if (currentSubjectCourseType == HOT_COURSE) sortCoursesByJoined(courses);
                else sortCoursesByAddTime(courses);

                JSONObject subjectJson = (JSONObject) JSON.toJSON(subject);
                subjectJson.put("courses", courses.subList(0, Math.min(courses.size(), 3)));
                subjectsJson.add(subjectJson);

                subjectJson.put("subjectCourseType", currentSubjectCourseType);

                if (currentSubjectCourseType == HOT_COURSE)  subjectJson.put("coursesTitle", "本周热门课程");
                else subjectJson.put("coursesTitle", "本周新开课程");
            }
            indexJson.put("subjects", subjectsJson);

            List<DiscussTopic> topics = discussServiceApi.listTopics(cityId, 0, subjectsJson.size()).getList();
            for (DiscussTopic topic : topics) {
                topic.setCover(completeLargeImg(topic.getCover()));
            }
            indexJson.put("topics", topics);
        }

        indexJson.put("courses", getRecommendCourses(cityId, start));

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    private void sortCoursesByJoined(List<Course> courses) {
        if (courses.isEmpty()) return;
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course c1, Course c2) {
                return c2.getJoined() - c1.getJoined();
            }
        });
    }

    private void sortCoursesByAddTime(List<Course> courses) {
        if (courses.isEmpty()) return;
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course c1, Course c2) {
                return (int) (c2.getAddTime().getTime() - c1.getAddTime().getTime());
            }
        });
    }

    private PagedList<Course> getRecommendCourses(int cityId, int start) {
        try {
            PagedList<Course> courses = courseServiceApi.listRecommend(cityId, start, Configuration.getInt("PageSize.Course"));
            for (Course course : courses.getList()) {
                completeLargeImg(course);
            }

            return courses;
        } catch (Exception e) {
            LOGGER.error("fail to list recommend courses", e);
            return PagedList.EMPTY;
        }
    }
}
