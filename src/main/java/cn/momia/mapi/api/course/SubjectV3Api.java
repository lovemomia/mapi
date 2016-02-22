package cn.momia.mapi.api.course;

import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.course.dto.subject.Subject;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v3/subject")
public class SubjectV3Api extends AbstractApi {
    @Autowired
    private SubjectServiceApi subjectServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程体系ID");

        Subject subject = subjectServiceApi.get(id);
        completeLargeImg(subject);

        List<UserCourseComment> comments = subjectServiceApi.queryRecommendedCommentsBySubject(id, 0, 2);
        completeCourseCommentsImgs(comments);

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }
}
