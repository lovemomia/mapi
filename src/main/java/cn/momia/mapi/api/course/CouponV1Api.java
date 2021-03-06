package cn.momia.mapi.api.course;

import cn.momia.api.course.CouponServiceApi;
import cn.momia.api.course.dto.coupon.Coupon;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/coupon")
public class CouponV1Api extends AbstractApi {
    @Autowired private CouponServiceApi couponServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getCoupon(@RequestParam int id) {
        Coupon coupon = couponServiceApi.get(id);
        if (!coupon.exists()) return MomiaHttpResponse.FAILED("无效的红包");
        return MomiaHttpResponse.SUCCESS(coupon);
    }

    @RequestMapping(value = "/share", method = RequestMethod.GET)
    public MomiaHttpResponse shareCoupon(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        User user = userServiceApi.get(utoken);
        JSONObject shareJson = new JSONObject();
        shareJson.put("img", Configuration.getString("Share.Img"));
        shareJson.put("desc", Configuration.getString("Share.Desc"));
        shareJson.put("url", Configuration.getString("Share.Url") + "?invite=" + user.getInviteCode());
        shareJson.put("cover", Configuration.getString("Share.Cover"));
        shareJson.put("title", Configuration.getString("Share.Title"));
        shareJson.put("abstracts", Configuration.getString("Share.Abstracts"));

        return MomiaHttpResponse.SUCCESS(shareJson);
    }

    @RequestMapping(value = "/invite", method = RequestMethod.POST)
    public MomiaHttpResponse inviteCoupon(@RequestParam String mobile, @RequestParam(value = "invite") String inviteCode) {
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(inviteCode)) return MomiaHttpResponse.FAILED("无效的邀请码");
        if (userServiceApi.getByMobile(mobile).exists()) return MomiaHttpResponse.FAILED("该手机号已经注册过，只有新用户才能领取");

        couponServiceApi.invite(mobile, inviteCode);
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/code", method = RequestMethod.GET)
    public MomiaHttpResponse couponCode(@RequestParam String code) {
        if (StringUtils.isBlank(code)) return MomiaHttpResponse.FAILED("无效的优惠码");
        return MomiaHttpResponse.SUCCESS(couponServiceApi.couponCode(code));
    }
}
