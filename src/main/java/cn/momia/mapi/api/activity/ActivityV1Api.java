package cn.momia.mapi.api.activity;

import cn.momia.api.course.ActivityServiceApi;
import cn.momia.api.course.activity.Activity;
import cn.momia.api.course.activity.ActivityEntry;
import cn.momia.api.user.SmsServiceApi;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.mapi.api.AbstractApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/activity")
public class ActivityV1Api extends AbstractApi {
    @Autowired private ActivityServiceApi activityServiceApi;
    @Autowired private SmsServiceApi smsServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam int id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的活动ID");

        Activity activity = activityServiceApi.get(id);
        activity.setIcon(completeSmallImg(activity.getIcon()));
        activity.setCover(completeLargeImg(activity.getCover()));

        return MomiaHttpResponse.SUCCESS(activity);
    }

    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public MomiaHttpResponse join(@RequestParam(value = "aid") int activityId,
                                  @RequestParam String mobile,
                                  @RequestParam(value = "cname") String childName,
                                  @RequestParam(required = false, defaultValue = "") String relation,
                                  @RequestParam(required = false, defaultValue = "") String extra) {
        if (activityId <= 0) return MomiaHttpResponse.FAILED("无效的活动");
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(childName)) return MomiaHttpResponse.FAILED("孩子姓名不能为空");

        return MomiaHttpResponse.SUCCESS(activityServiceApi.join(activityId, mobile, childName, relation, extra));
    }

    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public MomiaHttpResponse notify(@RequestParam(value = "aid") int activityId,
                                    @RequestParam String mobile,
                                    @RequestParam(value = "cname", required = false, defaultValue = "") String childName) {
        if (activityId <= 0) return MomiaHttpResponse.FAILED("无效的活动");
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");

        Activity activity = activityServiceApi.get(activityId);
        if (!activity.exists()) return MomiaHttpResponse.FAILED("无效的活动");

        if (!StringUtils.isBlank(childName)) {
            ActivityEntry activityEntry = activityServiceApi.getEntry(activityId, mobile, childName);
            if (!activityEntry.exists()) return MomiaHttpResponse.FAILED("您还没有报名成功");

            return MomiaHttpResponse.SUCCESS(smsServiceApi.notify(mobile, activity.getMessage() + activityEntry.getExtraMessage()));
        } else {
            return MomiaHttpResponse.SUCCESS(smsServiceApi.notify(mobile, activity.getMessage()));
        }
    }

    @RequestMapping(value = "/prepay/alipay", method = RequestMethod.POST)
    public MomiaHttpResponse prepayAlipay(@RequestParam(value = "eid") long entryId, @RequestParam(defaultValue = "app") String type) {
        if (entryId <= 0) return MomiaHttpResponse.FAILED("无效的报名ID");
        if (StringUtils.isBlank(type)) return MomiaHttpResponse.FAILED("无效的类型");

        return MomiaHttpResponse.SUCCESS(activityServiceApi.prepayAlipay(entryId, type));
    }

    @RequestMapping(value = "/prepay/weixin", method = RequestMethod.POST)
    public MomiaHttpResponse prepayWeixin(@RequestParam(value = "eid") long entryId,
                                          @RequestParam(defaultValue = "app") final String type,
                                          @RequestParam(required = false) String code) {
        if (entryId <= 0) return MomiaHttpResponse.FAILED("无效的报名ID");
        if (StringUtils.isBlank(type)) return MomiaHttpResponse.FAILED("无效的类型");

        return MomiaHttpResponse.SUCCESS(activityServiceApi.prepayWeixin(entryId, type, code));
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public MomiaHttpResponse check(@RequestParam(value = "eid") long entryId) {
        if (entryId <= 0) return MomiaHttpResponse.FAILED("无效的报名ID");
        return MomiaHttpResponse.SUCCESS(activityServiceApi.checkPayment(entryId));
    }

    @RequestMapping(value = "/coupon", method = RequestMethod.POST)
    public MomiaHttpResponse getCoupon(@RequestParam String utoken, @RequestParam(value = "coupon") int couponId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (couponId <= 0) return MomiaHttpResponse.FAILED("无效的红包id");
        return MomiaHttpResponse.SUCCESS(activityServiceApi.getCoupon(utoken, couponId));
    }

    @RequestMapping(value = "/coupons", method = RequestMethod.POST)
    public MomiaHttpResponse getCoupons(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(activityServiceApi.getCoupons(utoken));
    }
}
