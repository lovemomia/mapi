package cn.momia.mapi.api.v1.teacher;

import cn.momia.api.teacher.TeacherServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/teacher")
public class TeacherV1Api {
    @Autowired private TeacherServiceApi teacherServiceApi;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public MomiaHttpResponse status(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(teacherServiceApi.status(utoken));
    }
}
