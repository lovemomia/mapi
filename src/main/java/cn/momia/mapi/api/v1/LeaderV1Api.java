package cn.momia.mapi.api.v1;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.leader.Leader;
import cn.momia.api.user.leader.LeaderStatus;
import cn.momia.api.user.User;
import cn.momia.common.webapp.config.Configuration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public MomiaHttpResponse getStatus(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject statusJson = new JSONObject();

        LeaderStatus leaderStatus = UserServiceApi.LEADER.getStatus(utoken);
        statusJson.put("status", leaderStatus.getStatus());
        statusJson.put("msg", leaderStatus.getMsg());

        switch (leaderStatus.getStatus()) {
            case Status.PASSED:
                MomiaHttpResponse ledProductsResponse = getLedProducts(utoken, 0);
                if (!ledProductsResponse.isSuccessful()) return MomiaHttpResponse.FAILED("获取领队状态失败");

                statusJson.put("products", ledProductsResponse.getData());
                break;
            case Status.NOTEXIST:
            case Status.AUDITING:
                statusJson.put("desc", JSON.parseObject(Configuration.getString("Leader.Desc")));
                break;
            default: break;
        }

        return MomiaHttpResponse.SUCCESS(statusJson);
    }

    @RequestMapping(value = "/product", method = RequestMethod.GET)
    public MomiaHttpResponse getLedProducts(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = UserServiceApi.USER.get(utoken);
        return MomiaHttpResponse.SUCCESS(processPagedProducts(ProductServiceApi.SKU.getLedProducts(user.getId(), start, Configuration.getInt("PageSize.Leader.Product")), IMAGE_MIDDLE));
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public MomiaHttpResponse apply(@RequestParam String utoken, @RequestParam(value = "pid") long productId, @RequestParam(value = "sid") long skuId) {
        if (StringUtils.isBlank(utoken) || productId <= 0 || skuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Leader leader = UserServiceApi.LEADER.get(utoken);
        if (leader.getStatus() != Status.PASSED) return MomiaHttpResponse.FAILED("您注册成为领队的请求还没通过审核，暂时不能当领队");

        ProductServiceApi.SKU.applyLeader(leader.getUserId(), productId, skuId);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public MomiaHttpResponse add(@RequestParam String utoken, @RequestParam String leader) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(leader)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject leaderJson = JSON.parseObject(leader);
        leaderJson.put("userId", UserServiceApi.USER.get(utoken).getId());
        UserServiceApi.LEADER.add(JSON.toJavaObject(leaderJson, Leader.class));

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public MomiaHttpResponse update(@RequestParam String utoken, @RequestParam String leader) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(leader)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject leaderJson = JSON.parseObject(leader);
        leaderJson.put("userId", UserServiceApi.USER.get(utoken).getId());
        UserServiceApi.LEADER.update(JSON.toJavaObject(leaderJson, Leader.class));

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse delete(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.BAD_REQUEST;

        UserServiceApi.LEADER.delete(utoken);

        return MomiaHttpResponse.SUCCESS;
    }
}
