package cn.momia.mapi.api.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.subject.Subject;
import cn.momia.api.course.dto.subject.SubjectSku;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Contact;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
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
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/trial", method = RequestMethod.GET)
    public MomiaHttpResponse trial(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0) return MomiaHttpResponse.FAILED("无效的城市ID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");
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
    public MomiaHttpResponse get(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程体系ID");

        Subject subject = subjectServiceApi.get(id);
        completeLargeImg(subject);

        PagedList<Course> courses = courseServiceApi.query(id, 0, Configuration.getInt("PageSize.Course"));
        completeLargeCoursesImgs(courses.getList());

        PagedList<UserCourseComment> comments = subjectServiceApi.queryCommentsBySubject(id, 0, Configuration.getInt("PageSize.CourseComment"));
        completeCourseCommentsImgs(comments.getList());

        JSONObject subjectJson = (JSONObject) JSON.toJSON(subject);
        SubjectSku cheapestSku = subject.getCheapestSku();
        if (cheapestSku == null) return MomiaHttpResponse.FAILED("无效的课程体系");
        subjectJson.put("cheapestSkuPrice", cheapestSku.getPrice());
        subjectJson.put("cheapestSkuTimeUnit", TimeUtil.toUnitString(cheapestSku.getTimeUnit()));
        subjectJson.put("cheapestSkuDesc", "任选" + MomiaUtil.CHINESE_NUMBER_CHARACTER[cheapestSku.getCourseCount()] + "门");

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subjectJson);
        responseJson.put("courses", courses);
        responseJson.put("feeds", PagedList.EMPTY);
        responseJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/sku", method = RequestMethod.GET)
    public MomiaHttpResponse sku(@RequestParam String utoken, @RequestParam long id, @RequestParam(required = false, value = "coid", defaultValue = "0") long courseId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程体系ID");

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
