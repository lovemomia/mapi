package cn.momia.mapi.api.v1.course;

import cn.momia.api.course.PaymentServiceApi;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.XmlUtil;
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
@RequestMapping("/v1/payment")
public class PaymentV1Api extends AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentV1Api.class);

    @Autowired private PaymentServiceApi paymentServiceApi;

    @RequestMapping(value = "/prepay/alipay", method = RequestMethod.POST)
    public MomiaHttpResponse prepayAlipay(@RequestParam String utoken,
                                          @RequestParam(value = "oid") long orderId,
                                          @RequestParam(defaultValue = "app") String type,
                                          @RequestParam(value = "coupon", required = false, defaultValue = "0") long userCouponId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || StringUtils.isBlank(type)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(paymentServiceApi.prepayAlipay(utoken, orderId, type, userCouponId));
    }

    @RequestMapping(value = "/prepay/weixin", method = RequestMethod.POST)
    public MomiaHttpResponse prepayWeixin(@RequestParam String utoken,
                                          @RequestParam(value = "oid") long orderId,
                                          @RequestParam(defaultValue = "app") final String type,
                                          @RequestParam(required = false) String code,
                                          @RequestParam(value = "coupon", required = false, defaultValue = "0") long userCouponId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || StringUtils.isBlank(type)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(paymentServiceApi.prepayWeixin(utoken, orderId, type, code, userCouponId));
    }

    @RequestMapping(value = "/callback/alipay", method = RequestMethod.POST, produces = "text/plain")
    public String callbackAlipay(HttpServletRequest request) {
        try {
            Map<String, String> params = extractParams(request);
            if (paymentServiceApi.callbackAlipay(params)) return "success";
        } catch (Exception e) {
            LOGGER.error("ali pay callback error", e);
        }

        LOGGER.error("ali pay callback failure");

        return "fail";
    }

    @RequestMapping(value = "/callback/weixin", method = RequestMethod.POST, produces = "application/xml")
    public String callbackWeixin(HttpServletRequest request) {
        try {
            Map<String, String> params = XmlUtil.xmlToMap(IOUtils.toString(request.getInputStream()));
            if (paymentServiceApi.callbackWeixin(params)) return WechatpayResponse.SUCCESS;
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
    public MomiaHttpResponse check(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(paymentServiceApi.checkPayment(utoken, orderId));
    }
}
