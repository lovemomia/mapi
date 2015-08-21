package cn.momia.mapi.api.v1;

import cn.momia.api.product.Product;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.deal.DealServiceApi;
import cn.momia.api.product.ProductServiceApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payment")
public class PaymentV1Api extends AbstractV1Api {
    @RequestMapping(value = "/prepay/alipay", method = RequestMethod.POST)
    public ResponseMessage prepayAlipay(@RequestParam String utoken,
                                        @RequestParam(value = "oid") long orderId,
                                        @RequestParam(value = "pid") long productId,
                                        @RequestParam(value = "sid") long skuId,
                                        @RequestParam(defaultValue = "app") String type,
                                        @RequestParam(required = false, defaultValue = "") String terminal,
                                        @RequestParam(required = false) Long coupon) {
        if (StringUtils.isBlank(utoken) ||
                orderId <= 0 ||
                productId <= 0 ||
                skuId <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(DealServiceApi.PAYMENT.prepayAlipay(utoken, orderId, productId, skuId, type, terminal, coupon));
    }

    @RequestMapping(value = "/prepay/wechatpay", method = RequestMethod.POST)
    public ResponseMessage prepayWechatpay(@RequestParam String utoken,
                                           @RequestParam(value = "oid") long orderId,
                                           @RequestParam(value = "pid") long productId,
                                           @RequestParam(value = "sid") long skuId,
                                           @RequestParam(value = "trade_type") final String tradeType,
                                           @RequestParam(required = false) Long coupon,
                                           @RequestParam(required = false) String code) {
        if (StringUtils.isBlank(utoken) ||
                orderId <= 0 ||
                productId <= 0 ||
                skuId <= 0 ||
                StringUtils.isBlank(tradeType)) return ResponseMessage.BAD_REQUEST;

        if (tradeType.equals("JSAPI") && StringUtils.isBlank(code)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(DealServiceApi.PAYMENT.prepayWechatpay(utoken, orderId, productId, skuId, tradeType, coupon, code));
    }

    @RequestMapping(value = "/prepay/free", method = RequestMethod.POST)
    public ResponseMessage prepayFree(@RequestParam String utoken,
                                      @RequestParam(value = "oid") long orderId,
                                      @RequestParam(value = "pid") long productId,
                                      @RequestParam(value = "sid") long skuId,
                                      @RequestParam(required = false) Long coupon) {
        if (StringUtils.isBlank(utoken) ||
                orderId <= 0 ||
                productId <= 0 ||
                skuId <= 0) return ResponseMessage.BAD_REQUEST;

        DealServiceApi.PAYMENT.prepayFree(utoken, orderId, productId, skuId, coupon);

        return ResponseMessage.SUCCESS(processProduct(ProductServiceApi.PRODUCT.get(productId, Product.Type.MINI), utoken));
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseMessage checkPayment(@RequestParam String utoken,
                                        @RequestParam(value = "oid") long orderId,
                                        @RequestParam(value = "pid") long productId,
                                        @RequestParam(value = "sid") long skuId) {
        if (StringUtils.isBlank(utoken) ||
                orderId <= 0 ||
                productId <= 0 ||
                skuId <= 0) return ResponseMessage.BAD_REQUEST;

        DealServiceApi.PAYMENT.check(utoken, orderId, productId, skuId);

        return ResponseMessage.SUCCESS(processProduct(ProductServiceApi.PRODUCT.get(productId, Product.Type.MINI), utoken));
    }
}
