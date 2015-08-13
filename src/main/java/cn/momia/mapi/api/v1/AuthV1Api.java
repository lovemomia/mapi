package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.http.MomiaHttpParamBuilder;
import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.mapi.common.util.MobileUtil;
import cn.momia.service.common.api.CommonServiceApi;
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

    @Autowired CommonServiceApi commonServiceApi;

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

        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder()
                .add("nickname", nickName)
                .add("mobile", mobile)
                .add("password", password)
                .add("code", code);
        MomiaHttpRequest request = MomiaHttpRequest.POST(url("auth/register"), builder.build());

        return executeRequest(request, userFunc);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseMessage login(@RequestParam String mobile, @RequestParam String password) {
        LOGGER.info("login by password: {}", mobile);

        if (MobileUtil.isInvalidMobile(mobile) || StringUtils.isBlank(password)) return ResponseMessage.BAD_REQUEST;

        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder()
                .add("mobile", mobile)
                .add("password", password);
        MomiaHttpRequest request = MomiaHttpRequest.POST(url("auth/login"), builder.build());

        return executeRequest(request, userFunc);
    }

    @RequestMapping(value = "/login/code", method = RequestMethod.POST)
    public ResponseMessage loginByCode(@RequestParam String mobile, @RequestParam String code) {
        LOGGER.info("login by code: {}", mobile);

        if (MobileUtil.isInvalidMobile(mobile) || StringUtils.isBlank(code)) return ResponseMessage.BAD_REQUEST;

        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder()
                .add("mobile", mobile)
                .add("code", code);
        MomiaHttpRequest request = MomiaHttpRequest.POST(url("auth/login/code"), builder.build());

        return executeRequest(request, userFunc);
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public ResponseMessage updatePassword(@RequestParam String mobile, @RequestParam String password, @RequestParam String code) {
        if (MobileUtil.isInvalidMobile(mobile) || StringUtils.isBlank(password) || StringUtils.isBlank(code)) return ResponseMessage.BAD_REQUEST;

        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder()
                .add("mobile", mobile)
                .add("password", password)
                .add("code", code);
        MomiaHttpRequest request = MomiaHttpRequest.PUT(url("auth/password"), builder.build());

        return executeRequest(request, userFunc);
    }
}
