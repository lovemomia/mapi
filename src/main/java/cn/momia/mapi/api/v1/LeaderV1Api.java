package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.leader.Leader;
import cn.momia.api.user.leader.LeaderStatus;
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
@RequestMapping("/v1/leader")
public class LeaderV1Api extends AbstractV1Api {
    public static class Status {
        public static final int NOTEXIST = 0;
        public static final int PASSED = 1;
        public static final int AUDITING = 2;
        public static final int REJECTED = 3;
    }

    @Autowired private UserServiceApi userServiceApi;
    @Autowired private ProductServiceApi productServiceApi;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseMessage getStatus(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return ResponseMessage.BAD_REQUEST;

        JSONObject statusJson = new JSONObject();

        LeaderStatus leaderStatus = userServiceApi.LEADER.getStatus(utoken);
        statusJson.put("status", leaderStatus.getStatus());
        statusJson.put("msg", leaderStatus.getMsg());

        switch (leaderStatus.getStatus()) {
            case Status.PASSED:
                ResponseMessage ledProductsResponse = getLedProducts(utoken, 0);
                if (!ledProductsResponse.successful()) return ResponseMessage.FAILED("获取领队状态失败");

                statusJson.put("products", ledProductsResponse.getData());
                break;
            case Status.NOTEXIST:
            case Status.AUDITING:
                statusJson.put("desc", JSON.parseObject(Configuration.getString("Leader.Desc")));
                break;
            default: break;
        }

        return ResponseMessage.SUCCESS(statusJson);
    }

    @RequestMapping(value = "/product", method = RequestMethod.GET)
    public ResponseMessage getLedProducts(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || start < 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        return ResponseMessage.SUCCESS(processPagedProducts(productServiceApi.SKU.getLedProducts(user.getId(), start, Configuration.getInt("PageSize.Leader.Product"))));
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public ResponseMessage apply(@RequestParam String utoken, @RequestParam(value = "pid") long productId, @RequestParam(value = "sid") long skuId) {
        if (StringUtils.isBlank(utoken) || productId <= 0 || skuId <= 0) return ResponseMessage.BAD_REQUEST;

        Leader leader = userServiceApi.LEADER.get(utoken);
        if (leader.getStatus() != Status.PASSED) return ResponseMessage.FAILED("您注册成为领队的请求还没通过审核，暂时不能当领队");

        productServiceApi.SKU.applyLeader(leader.getUserId(), productId, skuId);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseMessage add(@RequestParam String utoken, @RequestParam String leader) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(leader)) return ResponseMessage.BAD_REQUEST;

        JSONObject leaderJson = JSON.parseObject(leader);
        leaderJson.put("userId", userServiceApi.USER.get(utoken));
        userServiceApi.LEADER.add(JSON.toJavaObject(leaderJson, Leader.class));

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseMessage update(@RequestParam String utoken, @RequestParam String leader) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(leader)) return ResponseMessage.BAD_REQUEST;

        JSONObject leaderJson = JSON.parseObject(leader);
        leaderJson.put("userId", userServiceApi.USER.get(utoken));
        userServiceApi.LEADER.update(JSON.toJavaObject(leaderJson, Leader.class));

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseMessage delete(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return ResponseMessage.BAD_REQUEST;

        userServiceApi.LEADER.delete(utoken);

        return ResponseMessage.SUCCESS;
    }
}
