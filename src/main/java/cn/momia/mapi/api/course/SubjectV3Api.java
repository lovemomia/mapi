package cn.momia.mapi.api.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.subject.Subject;
import cn.momia.api.course.dto.subject.SubjectSku;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.FeedRelatedApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v3/subject")
public class SubjectV3Api extends FeedRelatedApi {
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private CourseServiceApi courseServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程体系ID");

        Subject subject = subjectServiceApi.get(id);
        subject.setCourses(null); // 后面通过course的接口单独取
        completeLargeImg(subject);

        List<Course> newCourses = courseServiceApi.listRecentCoursesBySubject(id);
        completeMiddleCoursesImgs(newCourses);

        List<Course> courses = courseServiceApi.listBySubject(id);
        completeLargeCoursesImgs(courses);

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
        responseJson.put("newCourses", newCourses);
        responseJson.put("courses", courses);
        responseJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }
}
