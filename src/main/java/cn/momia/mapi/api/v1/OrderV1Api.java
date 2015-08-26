package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.deal.DealServiceApi;
import cn.momia.api.user.UserServiceApi;
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
    public ResponseMessage placeOrder(@RequestParam String utoken,
                                      @RequestParam String order,
                                      @RequestParam(required = false, defaultValue = "") String invite) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(order)) return ResponseMessage.BAD_REQUEST;

        JSONObject orderJson = JSON.parseObject(order);
        orderJson.put("customerId", UserServiceApi.USER.get(utoken).getId());
        if (!StringUtils.isBlank(invite)) orderJson.put("inviteCode", invite);

        return ResponseMessage.SUCCESS(processOrder(DealServiceApi.ORDER.add(orderJson)));
    }

    @RequestMapping(value = "/check/dup", method = RequestMethod.GET)
    public ResponseMessage checkDup(@RequestParam String utoken, @RequestParam String order) {
        return ResponseMessage.SUCCESS(DealServiceApi.ORDER.checkDup(utoken, order));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseMessage deleteOrder(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        DealServiceApi.ORDER.delete(utoken, id);

        return ResponseMessage.SUCCESS;
    }
}
