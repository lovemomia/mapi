package cn.momia.mapi.api.v1.course;

import cn.momia.api.course.CouponServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.util.MobileUtil;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/coupon")
public class CouponV1Api extends AbstractV1Api {
    @Autowired private CouponServiceApi couponServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/invite", method = RequestMethod.POST)
    public MomiaHttpResponse inviteCoupon(@RequestParam String mobile, @RequestParam(value = "invite") String inviteCode) {
        if (MobileUtil.isInvalid(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(inviteCode)) return MomiaHttpResponse.BAD_REQUEST;
        if (userServiceApi.getByMobile(mobile).exists()) return MomiaHttpResponse.FAILED("该手机号已经注册过，只有新用户才能领取");

        couponServiceApi.invite(mobile, inviteCode);
        return MomiaHttpResponse.SUCCESS;
    }
}
