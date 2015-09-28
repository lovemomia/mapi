package cn.momia.mapi.api.v1.product;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.dto.ProductDto;
import cn.momia.api.product.DealServiceApi;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payment")
public class PaymentV1Api extends AbstractV1Api {
    @RequestMapping(value = "/prepay/alipay", method = RequestMethod.POST)
    public MomiaHttpResponse prepayAlipay(@RequestParam String utoken,
                                          @RequestParam(value = "oid") long orderId,
                                          @RequestParam(value = "pid") long productId,
                                          @RequestParam(value = "sid") long skuId,
                                          @RequestParam(defaultValue = "app") String type,
                                          @RequestParam(required = false) Long coupon) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || productId <= 0 || skuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(DealServiceApi.PAYMENT.prepayAlipay(utoken, orderId, productId, skuId, type, coupon));
    }

    @RequestMapping(value = "/prepay/wechatpay", method = RequestMethod.POST)
    public MomiaHttpResponse prepayWechatpay(@RequestParam String utoken,
                                             @RequestParam(value = "oid") long orderId,
                                             @RequestParam(value = "pid") long productId,
                                             @RequestParam(value = "sid") long skuId,
                                             @RequestParam(value = "trade_type") final String tradeType,
                                             @RequestParam(required = false) Long coupon,
                                             @RequestParam(required = false) String code) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || productId <= 0 || skuId <= 0 || StringUtils.isBlank(tradeType)) return MomiaHttpResponse.BAD_REQUEST;
        if (tradeType.equals("JSAPI") && StringUtils.isBlank(code)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(DealServiceApi.PAYMENT.prepayWechatpay(utoken, orderId, productId, skuId, tradeType, coupon, code));
    }

    @RequestMapping(value = "/prepay/free", method = RequestMethod.POST)
    public MomiaHttpResponse prepayFree(@RequestParam String utoken,
                                        @RequestParam(value = "oid") long orderId,
                                        @RequestParam(value = "pid") long productId,
                                        @RequestParam(value = "sid") long skuId,
                                        @RequestParam(required = false) Long coupon) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || productId <= 0 || skuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        DealServiceApi.PAYMENT.prepayFree(utoken, orderId, productId, skuId, coupon);
        return MomiaHttpResponse.SUCCESS(processProduct(ProductServiceApi.PRODUCT.get(productId, ProductDto.Type.MINI), utoken));
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public MomiaHttpResponse checkPayment(@RequestParam String utoken,
                                          @RequestParam(value = "oid") long orderId,
                                          @RequestParam(value = "pid") long productId,
                                          @RequestParam(value = "sid") long skuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || productId <= 0 || skuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        DealServiceApi.PAYMENT.check(utoken, orderId, productId, skuId);
        return MomiaHttpResponse.SUCCESS(processProduct(ProductServiceApi.PRODUCT.get(productId, ProductDto.Type.MINI), utoken));
    }
}
