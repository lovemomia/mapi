package cn.momia.mapi.api.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/vipcard")
public class VipCardV1Api extends AbstractApi {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public MomiaHttpResponse register(@RequestParam String utoken, @RequestParam String card, @RequestParam String password) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(card)) return MomiaHttpResponse.FAILED("卡号不能为空");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("卡密码不能为空");

        User user = userServiceApi.get(utoken);
        if (!courseServiceApi.registerVipCard(user.getId(), card, password)) return MomiaHttpResponse.FAILED("无效的卡号或密码");
        return MomiaHttpResponse.SUCCESS;
    }
}
