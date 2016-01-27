package cn.momia.mapi.api.index;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.subject.Subject;
import cn.momia.api.discuss.DiscussServiceApi;
import cn.momia.api.discuss.dto.DiscussTopic;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/v3/index")
public class IndexV3Api extends AbstractIndexApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexV2Api.class);

    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private DiscussServiceApi discussServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse index(HttpServletRequest request,
                                   @RequestParam(value = "city") int cityId,
                                   @RequestParam int start) {
        if (cityId < 0) return MomiaHttpResponse.FAILED("无效的CityID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        JSONObject indexJson = new JSONObject();
        if (start == 0) {
            int platform = getPlatform(request);
            String version = getVersion(request);

            indexJson.put("banners", getBanners(cityId, platform, version));
            indexJson.put("subjects", getSubjects(cityId));

            PagedList<DiscussTopic> topics = discussServiceApi.listTopics(cityId, 0, 1);
            if (topics.getList() != null && !topics.getList().isEmpty()) {
                DiscussTopic topic = topics.getList().get(0);
                topic.setCover(completeLargeImg(topic.getCover()));
                indexJson.put("topic", topic);
            }
        }

        indexJson.put("courses", getRecommendCourses(cityId, start));

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    private List<Subject> getSubjects(int cityId) {
        List<Subject> subjects = subjectServiceApi.list(cityId);
        for (Subject subject : subjects) {
            completeLargeImg(subject);
        }

        return subjects;
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
