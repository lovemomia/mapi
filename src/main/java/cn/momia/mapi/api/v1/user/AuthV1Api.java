package cn.momia.mapi.api.v1.user;

import cn.momia.api.base.SmsServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.DealServiceApi;
import cn.momia.api.user.dto.UserDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.util.MobileUtil;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthV1Api.class);

    @Autowired private SmsServiceApi smsServiceApi;

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

        UserDto user = processUser(UserServiceApi.USER.register(nickName, mobile, password, code));

        // TODO 可配置
        distributeRegisterCoupon(user);

        return MomiaHttpResponse.SUCCESS(user);
    }

    private void distributeRegisterCoupon(UserDto user) {
        try {
            DealServiceApi.COUPON.distributeRegisterCoupon(user.getToken());
        } catch (Exception e) {
            LOGGER.error("fail to distribute register coupons to user: {}", user.getId(), e);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public MomiaHttpResponse login(@RequestParam String mobile, @RequestParam String password) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.login(mobile, password)));
    }

    @RequestMapping(value = "/login/code", method = RequestMethod.POST)
    public MomiaHttpResponse loginByCode(@RequestParam String mobile, @RequestParam String code) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.loginByCode(mobile, code)));
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public MomiaHttpResponse updatePassword(@RequestParam String mobile, @RequestParam String password, @RequestParam String code) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updatePassword(mobile, password, code)));
    }
}
