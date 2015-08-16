package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.deal.DealServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/order")
public class OrderV1Api extends AbstractV1Api {
    @Autowired private UserServiceApi userServiceApi;
    @Autowired private DealServiceApi dealServiceApi;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseMessage placeOrder(@RequestParam String utoken, @RequestParam String order) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(order)) return ResponseMessage.BAD_REQUEST;

        JSONObject orderJson = JSON.parseObject(order);
        orderJson.put("customerId", userServiceApi.USER.get(utoken).getId());
        dealServiceApi.ORDER.add(orderJson);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseMessage deleteOrder(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        dealServiceApi.ORDER.delete(user.getId(), id);

        return ResponseMessage.SUCCESS;
    }
}
