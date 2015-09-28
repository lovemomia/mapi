package cn.momia.mapi.api.v1.product;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.DealServiceApi;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/coupon")
public class CouponV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse coupon(@RequestParam String utoken,
                                    @RequestParam(value = "oid") long orderId,
                                    @RequestParam long coupon) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || coupon <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(DealServiceApi.COUPON.calcTotalFee(utoken, orderId, coupon));
    }
}
