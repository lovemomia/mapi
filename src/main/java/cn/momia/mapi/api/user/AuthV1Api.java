package cn.momia.mapi.api.user;

import cn.momia.api.user.ChildServiceApi;
import cn.momia.api.user.SmsServiceApi;
import cn.momia.api.course.CouponServiceApi;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.user.AuthServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Child;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/auth")
public class AuthV1Api extends AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthV1Api.class);

    @Autowired private SmsServiceApi smsServiceApi;
    @Autowired private AuthServiceApi authServiceApi;
    @Autowired private CouponServiceApi couponServiceApi;
    @Autowired private ImServiceApi imServiceApi;
    @Autowired private UserServiceApi userServiceApi;
    @Autowired private ChildServiceApi childServiceApi;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public MomiaHttpResponse send(@RequestParam String mobile)  {
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (!smsServiceApi.send(mobile)) return MomiaHttpResponse.FAILED("发送短信验证码失败");

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public MomiaHttpResponse register(@RequestParam(value = "nickname") String nickName,
                                      @RequestParam String mobile,
                                      @RequestParam String password,
                                      @RequestParam String code) {
        if (StringUtils.isBlank(nickName)) return MomiaHttpResponse.FAILED("昵称不能为空");
        if (nickName.contains("官方")) return MomiaHttpResponse.FAILED("用户昵称不能包含“官方”");
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        User user = completeUserImgs(authServiceApi.register(nickName, mobile, password, code));
        distributeInviteCoupon(user.getId(), mobile);
        generateImToken(user, user.getNickName(), user.getAvatar());
        addDefaultChild(user);

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.get(user.getToken())));
    }

    private void distributeInviteCoupon(long userId, String mobile) {
        try {
            couponServiceApi.distributeInviteCoupon(userId, mobile);
        } catch (Exception e) {
            LOGGER.error("分发邀请红包失败", e);
        }
    }

    private void generateImToken(User user, String nickName, String avatar) {
        try {
            String imToken = imServiceApi.generateImToken(user.getId(), nickName, avatar);
            if (!StringUtils.isBlank(imToken)) userServiceApi.updateImToken(user.getToken(), imToken);
        } catch (Exception e) {
            LOGGER.error("fail to generate im token for user: {}", user.getId(), e);
        }
    }

    private void addDefaultChild(User user) {
        try {
            Child child = new Child();
            child.setUserId(user.getId());
            child.setName(user.getNickName() + "的宝宝");
            child.setSex("男");
            child.setBirthday(new Date(new Date().getTime() - 5 * 365 * 24 * 60 * 60 * 1000L));

            List<Child> children = new ArrayList<Child>();
            children.add(child);

            childServiceApi.add(user.getToken(), JSON.toJSONString(children));
        } catch (Exception e) {
            LOGGER.error("fail to add default child for user: {}", user.getId(), e);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public MomiaHttpResponse login(@RequestParam String mobile, @RequestParam String password) {
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(authServiceApi.login(mobile, password)));
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public MomiaHttpResponse updatePassword(@RequestParam String mobile, @RequestParam String password, @RequestParam String code) {
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(password)) return MomiaHttpResponse.FAILED("密码不能为空");
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("验证码不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(authServiceApi.updatePassword(mobile, password, code)));
    }
}
