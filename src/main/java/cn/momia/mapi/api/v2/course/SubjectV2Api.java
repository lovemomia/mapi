package cn.momia.mapi.api.v2.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.Course;
import cn.momia.api.course.dto.Subject;
import cn.momia.api.course.dto.SubjectSku;
import cn.momia.api.course.dto.UserCourseComment;
import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.UserFeed;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Contact;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v2/subject")
public class SubjectV2Api extends AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectV2Api.class);

    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private FeedServiceApi feedServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/trial", method = RequestMethod.GET)
    public MomiaHttpResponse trial(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(getTrialSubjects(cityId, start));
    }

    private PagedList<Course> getTrialSubjects(int cityId, int start) {
        try {
            PagedList<Course> courses = courseServiceApi.listTrial(cityId, start, Configuration.getInt("PageSize.Trial"));
            completeLargeCoursesImgs(courses.getList());

            return courses;
        } catch (Exception e) {
            LOGGER.error("fail to list trial courses", e);
            return PagedList.EMPTY;
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Subject subject = subjectServiceApi.get(id);
        completeLargeImg(subject);

        PagedList<Course> courses = courseServiceApi.query(id, 0, Configuration.getInt("PageSize.Course"));
        completeMiddleCoursesImgs(courses.getList());

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        PagedList<UserFeed> feeds = feedServiceApi.queryBySubject(userId, id, 0, Configuration.getInt("PageSize.Feed"));
        completeFeedsImgs(feeds.getList());

        PagedList<UserCourseComment> comments = subjectServiceApi.queryCommentsBySubject(id, 0, Configuration.getInt("PageSize.CourseComment"));
        completeCourseCommentsImgs(comments.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("courses", courses);
        responseJson.put("feeds", feeds);
        responseJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/sku", method = RequestMethod.GET)
    public MomiaHttpResponse sku(@RequestParam String utoken, @RequestParam long id, @RequestParam(required = false, value = "coid", defaultValue = "0") long courseId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<SubjectSku> skus = subjectServiceApi.querySkus(id);
        Contact contact = userServiceApi.getContact(utoken);

        List<SubjectSku> courseSkus = new ArrayList<SubjectSku>();
        List<SubjectSku> subjectSkus = new ArrayList<SubjectSku>();
        for (SubjectSku sku : skus) {
            if (sku.getCourseId() <= 0) {
                subjectSkus.add(sku);
            } else if (sku.getCourseId() == courseId) {
                courseSkus.add(sku);
            }
        }

        JSONObject responseJson = new JSONObject();
        if (courseId > 0) {
            responseJson.put("skus", courseSkus);
            responseJson.put("packages", subjectSkus);
        } else {
            responseJson.put("skus", subjectSkus);
        }
        responseJson.put("contact", contact);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }
}
