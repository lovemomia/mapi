package cn.momia.mapi.api.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.course.CourseDetail;
import cn.momia.api.course.dto.subject.SubjectSku;
import cn.momia.api.user.TeacherServiceApi;
import cn.momia.api.user.dto.Teacher;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v3/course")
public class CourseV3Api extends AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseV3Api.class);

    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private TeacherServiceApi teacherServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(required = false, defaultValue = "") String utoken,
                                 @RequestParam long id,
                                 @RequestParam(required = false, defaultValue = "") String pos) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");

        Course course = completeLargeImg(courseServiceApi.get(id, pos));
        JSONObject courseJson = (JSONObject) JSON.toJSON(course);

        PagedList<Integer> pagedTeacherIds = courseServiceApi.teacherIds(id, 0, Configuration.getInt("PageSize.Teacher"));
        List<Teacher> teachers = completeTeachersImgs(teacherServiceApi.list(pagedTeacherIds.getList()));
        if (!teachers.isEmpty()) courseJson.put("teachers", teachers);

        PagedList<UserCourseComment> pagedComments = courseServiceApi.queryCommentsByCourse(id, 0, Configuration.getInt("PageSize.CourseComment"));
        completeCourseCommentsImgs(pagedComments.getList());
        if (!pagedComments.getList().isEmpty()) courseJson.put("comments", pagedComments);

        List<SubjectSku> subjectSkus = subjectServiceApi.querySkus(course.getSubjectId());
        SubjectSku cheapestSubjectSku = null;
        for (SubjectSku subjectSku : subjectSkus) {
            if (subjectSku.getCourseId() > 0) continue;
            if (cheapestSubjectSku == null || subjectSku.getPrice().compareTo(cheapestSubjectSku.getPrice()) < 0) cheapestSubjectSku = subjectSku;
        }
        if (cheapestSubjectSku != null) {
            courseJson.put("cheapestSku", cheapestSubjectSku);
            courseJson.put("cheapestSkuPrice", cheapestSubjectSku.getPrice());
            courseJson.put("cheapestSkuTimeUnit", TimeUtil.toUnitString(cheapestSubjectSku.getTimeUnit()));
            courseJson.put("cheapestSkuDesc", "任选" + cheapestSubjectSku.getCourseCount() + "次");
        }

        try {
            CourseDetail detail = courseServiceApi.detail(id);
            JSONArray detailJson = JSON.parseArray(detail.getDetail());
            for (int i = 0; i < detailJson.size(); i++) {
                JSONObject detailBlockJson = detailJson.getJSONObject(i);
                JSONArray contentJson = detailBlockJson.getJSONArray("content");
                for (int j = 0; j < contentJson.size(); j++) {
                    JSONObject contentBlockJson = contentJson.getJSONObject(j);
                    if (contentBlockJson.containsKey("img")) contentBlockJson.put("img", completeLargeImg(contentBlockJson.getString("img")));
                }
            }
            courseJson.put("goal", detail.getAbstracts());
            courseJson.put("detail", detailJson);
        } catch (Exception e) {
            LOGGER.warn("invalid course detail: {}", id);
        }

        return MomiaHttpResponse.SUCCESS(courseJson);
    }
}
