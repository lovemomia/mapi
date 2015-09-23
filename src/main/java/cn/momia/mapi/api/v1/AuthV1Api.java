package cn.momia.mapi.api.v1;

import cn.momia.api.base.BaseServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.DealServiceApi;
import cn.momia.api.user.dto.UserDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.util.MobileUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthV1Api.class);

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public MomiaHttpResponse send(@RequestParam String mobile, @RequestParam(defaultValue = "login") String type)  {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码，请检查输入");

        BaseServiceApi.SMS.send(mobile, type);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public MomiaHttpResponse register(@RequestParam(value = "nickname") String nickName,
                                      @RequestParam String mobile,
                                      @RequestParam String password,
                                      @RequestParam String code) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码，请检查输入");
        if (StringUtils.isBlank(nickName) ||
                StringUtils.isBlank(password) ||
                StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("昵称、密码和验证码都不能为空");

        UserDto user = processUser(UserServiceApi.USER.register(nickName, mobile, password, code));

        try {
            DealServiceApi.COUPON.distributeRegisterCoupon(user.getToken());
        } catch (Exception e) {
            LOGGER.error("fail to distribute coupon to user: {}", user.getId(), e);
        }

        return MomiaHttpResponse.SUCCESS(user);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public MomiaHttpResponse login(@RequestParam String mobile, @RequestParam String password) {
        LOGGER.info("login by password: {}", mobile);

        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码，请检查输入");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.login(mobile, password)));
    }

    @RequestMapping(value = "/login/code", method = RequestMethod.POST)
    public MomiaHttpResponse loginByCode(@RequestParam String mobile, @RequestParam String code) {
        LOGGER.info("login by code: {}", mobile);

        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码，请检查输入");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.loginByCode(mobile, code)));
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public MomiaHttpResponse updatePassword(@RequestParam String mobile, @RequestParam String password, @RequestParam String code) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码，请检查输入");
        if (StringUtils.isBlank(password) || StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("密码和验证码都不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updatePassword(mobile, password, code)));
    }
}
