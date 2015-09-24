package cn.momia.mapi.api.v1.product;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.DealServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/order")
public class OrderV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse placeOrder(@RequestParam String utoken,
                                        @RequestParam String order,
                                        @RequestParam(required = false, defaultValue = "") String invite) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(order)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject orderJson = JSON.parseObject(order);
        orderJson.put("customerId", UserServiceApi.USER.get(utoken).getId());
        if (!StringUtils.isBlank(invite)) orderJson.put("inviteCode", invite);

        return MomiaHttpResponse.SUCCESS(processOrder(DealServiceApi.ORDER.add(orderJson)));
    }

    @RequestMapping(value = "/check/dup", method = RequestMethod.POST)
    public MomiaHttpResponse checkDup(@RequestParam String utoken, @RequestParam String order) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(order)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject orderJson = JSON.parseObject(order);
        orderJson.put("customerId", UserServiceApi.USER.get(utoken).getId());

        return MomiaHttpResponse.SUCCESS(DealServiceApi.ORDER.checkDup(orderJson));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteOrder(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        DealServiceApi.ORDER.delete(utoken, id);
        return MomiaHttpResponse.SUCCESS;
    }
}
