package cn.momia.mapi.api.activity;

import cn.momia.api.course.ActivityServiceApi;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.mapi.api.AbstractApi;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/v1/activity")
public class ActivityV1Api extends AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityV1Api.class);

    @Autowired private ActivityServiceApi activityServiceApi;

    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public MomiaHttpResponse join(@RequestParam(value = "aid") int activityId,
                                  @RequestParam String mobile,
                                  @RequestParam(value = "cname") String childName) {
        if (activityId <= 0) return MomiaHttpResponse.FAILED("无效的活动");
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(childName)) return MomiaHttpResponse.FAILED("孩子姓名不能为空");

        return MomiaHttpResponse.SUCCESS(activityServiceApi.join(activityId, mobile, childName));
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

    @RequestMapping(value = "/callback/alipay", method = RequestMethod.POST, produces = "text/plain")
    public String callbackAlipay(HttpServletRequest request) {
        try {
            Map<String, String> params = extractParams(request);
            if (activityServiceApi.callbackAlipay(params)) return "success";
        } catch (Exception e) {
            LOGGER.error("ali pay callback error", e);
        }

        LOGGER.error("ali pay callback failure");

        return "fail";
    }

    @RequestMapping(value = "/callback/weixin", method = RequestMethod.POST, produces = "application/xml")
    public String callbackWeixin(HttpServletRequest request) {
        try {
            Map<String, String> params = MomiaUtil.xmlToMap(IOUtils.toString(request.getInputStream()));
            if (activityServiceApi.callbackWeixin(params)) return WechatpayResponse.SUCCESS;
        } catch (Exception e) {
            LOGGER.error("wechat pay callback error", e);
        }

        LOGGER.error("wechat pay callback failure");

        return WechatpayResponse.FAILED;
    }

    private static class WechatpayResponse {
        public static String SUCCESS = new WechatpayResponse("SUCCESS", "OK").toString();
        public static String FAILED = new WechatpayResponse("FAIL", "ERROR").toString();

        private String return_code;
        private String return_msg;

        public WechatpayResponse(String return_code, String return_msg) {
            this.return_code = return_code;
            this.return_msg = return_msg;
        }

        @Override
        public String toString() {
            return "<xml><return_code>" + return_code + "</return_code><return_msg>" + return_msg + "</return_msg></xml>";
        }
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public MomiaHttpResponse check(@RequestParam(value = "eid") long entryId) {
        if (entryId <= 0) return MomiaHttpResponse.FAILED("无效的报名ID");

        return MomiaHttpResponse.SUCCESS(activityServiceApi.checkPayment(entryId));
    }
}
