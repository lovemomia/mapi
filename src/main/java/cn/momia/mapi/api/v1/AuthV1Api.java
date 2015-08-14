package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.mapi.common.util.MobileUtil;
import cn.momia.service.common.api.CommonServiceApi;
import cn.momia.service.user.api.UserServiceApi;
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

    @Autowired private CommonServiceApi commonServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseMessage send(@RequestParam String mobile, @RequestParam(defaultValue = "login") String type)  {
        if (MobileUtil.isInvalidMobile(mobile)) return ResponseMessage.BAD_REQUEST;

        commonServiceApi.SMS.send(mobile, type);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseMessage register(@RequestParam(value = "nickname") String nickName,
                                    @RequestParam String mobile,
                                    @RequestParam String password,
                                    @RequestParam String code) {
        if (StringUtils.isBlank(nickName) ||
                MobileUtil.isInvalidMobile(mobile) ||
                StringUtils.isBlank(password) ||
                StringUtils.isBlank(code)) return ResponseMessage.BAD_REQUEST;

        // TODO 发红包
        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.register(nickName, mobile, password, code)));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseMessage login(@RequestParam String mobile, @RequestParam String password) {
        LOGGER.info("login by password: {}", mobile);

        if (MobileUtil.isInvalidMobile(mobile) || StringUtils.isBlank(password)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.login(mobile, password)));
    }

    @RequestMapping(value = "/login/code", method = RequestMethod.POST)
    public ResponseMessage loginByCode(@RequestParam String mobile, @RequestParam String code) {
        LOGGER.info("login by code: {}", mobile);

        if (MobileUtil.isInvalidMobile(mobile) || StringUtils.isBlank(code)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.loginByCode(mobile, code)));
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public ResponseMessage updatePassword(@RequestParam String mobile, @RequestParam String password, @RequestParam String code) {
        if (MobileUtil.isInvalidMobile(mobile) || StringUtils.isBlank(password) || StringUtils.isBlank(code)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updatePassword(mobile, password, code)));
    }
}
