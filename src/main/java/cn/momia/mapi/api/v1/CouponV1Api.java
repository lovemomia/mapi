package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.deal.DealServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/coupon")
public class CouponV1Api extends AbstractV1Api {
    @Autowired private DealServiceApi dealServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage coupon(@RequestParam String utoken,
                                  @RequestParam(value = "oid") long orderId,
                                  @RequestParam long coupon) {
        if (StringUtils.isBlank(utoken) || orderId <= 0 || coupon <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        return ResponseMessage.SUCCESS(dealServiceApi.COUPON.calcTotalFee(user.getId(), orderId, coupon));
    }
}
