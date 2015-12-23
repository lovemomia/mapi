package cn.momia.mapi.api.v1.teacher;

import cn.momia.api.teacher.TeacherServiceApi;
import cn.momia.api.teacher.dto.Teacher;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.util.SexUtil;
import cn.momia.mapi.api.AbstractApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/v1/teacher")
public class TeacherV1Api extends AbstractApi {
    @Autowired private TeacherServiceApi teacherServiceApi;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public MomiaHttpResponse status(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(teacherServiceApi.status(utoken));
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public MomiaHttpResponse signup(@RequestParam String utoken, @RequestParam String teacher) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(teacher)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.signup(utoken, teacher));
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.get(utoken)));
    }

    private Teacher completeTeacherImgs(Teacher teacher) {
        teacher.setPic(completeSmallImg(teacher.getPic()));
        return teacher;
    }

    @RequestMapping(value = "/pic", method = RequestMethod.POST)
    public MomiaHttpResponse updatePic(@RequestParam String utoken, @RequestParam String pic) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(pic)) return MomiaHttpResponse.FAILED("照片不能为空");

        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.updatePic(utoken, pic)));
    }

    @RequestMapping(value = "/name", method = RequestMethod.POST)
    public MomiaHttpResponse updateName(@RequestParam String utoken, @RequestParam String name) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(name)) return MomiaHttpResponse.FAILED("名字不能为空");

        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.updateName(utoken, name)));
    }

    @RequestMapping(value = "/idno", method = RequestMethod.POST)
    public MomiaHttpResponse updateIdNo(@RequestParam String utoken, @RequestParam String idno) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(idno)) return MomiaHttpResponse.FAILED("身份证号码不能为空");

        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.updateIdNo(utoken, idno)));
    }

    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    public MomiaHttpResponse updateSex(@RequestParam String utoken, @RequestParam String sex) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (SexUtil.isInvalid(sex)) return MomiaHttpResponse.FAILED("无效的性别");

        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.updateSex(utoken, sex)));
    }

    @RequestMapping(value = "/birthday", method = RequestMethod.POST)
    public MomiaHttpResponse updateBirthday(@RequestParam String utoken, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (birthday == null) return MomiaHttpResponse.FAILED("生日不能为空");

        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.updateBirthday(utoken, birthday)));
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    public MomiaHttpResponse updateAddress(@RequestParam String utoken, @RequestParam String address) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(address)) return MomiaHttpResponse.FAILED("住址不能为空");

        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.updateAddress(utoken, address)));
    }
}
