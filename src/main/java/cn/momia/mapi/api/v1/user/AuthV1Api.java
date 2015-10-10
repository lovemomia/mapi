package cn.momia.mapi.api.v1.user;

import cn.momia.api.base.SmsServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.util.MobileUtil;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthV1Api extends AbstractV1Api {
    @Autowired private SmsServiceApi smsServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public MomiaHttpResponse send(@RequestParam String mobile)  {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (!smsServiceApi.send(mobile)) return MomiaHttpResponse.FAILED("发送短信验证码失败");

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public MomiaHttpResponse register(@RequestParam(value = "nickname") String nickName,
                                      @RequestParam String mobile,
                                      @RequestParam String password,
                                      @RequestParam String code) {
        if (StringUtils.isBlank(nickName)) return MomiaHttpResponse.FAILED("昵称不能为空");
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.register(nickName, mobile, password, code)));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public MomiaHttpResponse login(@RequestParam String mobile, @RequestParam String password) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.login(mobile, password)));
    }

    @RequestMapping(value = "/login/code", method = RequestMethod.POST)
    public MomiaHttpResponse loginByCode(@RequestParam String mobile, @RequestParam String code) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.loginByCode(mobile, code)));
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public MomiaHttpResponse updatePassword(@RequestParam String mobile, @RequestParam String password, @RequestParam String code) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updatePassword(mobile, password, code)));
    }
}