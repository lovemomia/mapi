package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.common.http.MomiaHttpParamBuilder;
import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.service.deal.api.DealServiceApi;
import cn.momia.service.product.api.ProductServiceApi;
import cn.momia.service.product.api.product.PagedProducts;
import cn.momia.service.user.api.UserServiceApi;
import cn.momia.service.user.api.participant.Participant;
import cn.momia.service.user.api.user.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/user")
public class UserV1Api extends AbstractV1Api {
    @Autowired private DealServiceApi dealServiceApi;
    @Autowired private ProductServiceApi productServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage getUser(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.get(utoken)));
    }

    @RequestMapping(value = "/nickname", method = RequestMethod.POST)
    public ResponseMessage updateNickName(@RequestParam String utoken, @RequestParam(value = "nickname") String nickName) {
        if(StringUtils.isBlank(utoken) || StringUtils.isBlank(nickName)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateNickName(utoken, nickName)));
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public ResponseMessage updateAvatar(@RequestParam String utoken, @RequestParam String avatar) {
        if(StringUtils.isBlank(utoken) || StringUtils.isBlank(avatar)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateAvatar(utoken, avatar)));
    }

    @RequestMapping(value = "/name", method = RequestMethod.POST)
    public ResponseMessage updateName(@RequestParam String utoken, @RequestParam String name) {
        if(StringUtils.isBlank(utoken) || StringUtils.isBlank(name)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateName(utoken, name)));
    }

    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    public ResponseMessage updateSex(@RequestParam String utoken, @RequestParam String sex) {
        if(StringUtils.isBlank(utoken) || StringUtils.isBlank(sex)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateSex(utoken, sex)));
    }

    @RequestMapping(value = "/birthday", method = RequestMethod.POST)
    public ResponseMessage updateBirthday(@RequestParam String utoken, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date birthday) {
        if(StringUtils.isBlank(utoken)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateBirthday(utoken, birthday)));
    }

    @RequestMapping(value = "/city", method = RequestMethod.POST)
    public ResponseMessage updateCity(@RequestParam String utoken, @RequestParam(value = "city") int cityId) {
        if(StringUtils.isBlank(utoken) || cityId <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateCity(utoken, cityId)));
    }

    @RequestMapping(value = "/region", method = RequestMethod.POST)
    public ResponseMessage updateRegion(@RequestParam String utoken, @RequestParam(value = "region") int regionId) {
        if(StringUtils.isBlank(utoken) || regionId <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateRegion(utoken, regionId)));
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    public ResponseMessage updateAddress(@RequestParam String utoken, @RequestParam String address) {
        if(StringUtils.isBlank(utoken) || StringUtils.isBlank(address)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateAddress(utoken, address)));
    }

    @RequestMapping(value = "/child", method = RequestMethod.POST)
    public ResponseMessage addChild(@RequestParam String utoken, @RequestParam String children) {
        if(StringUtils.isBlank(utoken) || StringUtils.isBlank(children)) return ResponseMessage.BAD_REQUEST;

        long userId = getUserId(utoken);
        List<Participant> participants = new ArrayList<Participant>();
        JSONArray childrenJson = JSONArray.parseArray(children);
        for (int i = 0; i < childrenJson.size(); i++) {
            Participant participant = JSON.toJavaObject(childrenJson.getJSONObject(i), Participant.class);
            participant.setUserId(userId);
            participants.add(participant);
        }

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.addChildren(participants)));
    }

    @RequestMapping(value = "/child", method = RequestMethod.GET)
    public ResponseMessage getChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if(StringUtils.isBlank(utoken) || childId <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(userServiceApi.USER.getChild(utoken, childId));
    }

    @RequestMapping(value = "/child/name", method = RequestMethod.POST)
    public ResponseMessage updateChildName(@RequestParam String utoken,
                                           @RequestParam(value = "cid") long childId,
                                           @RequestParam String name) {
        if (StringUtils.isBlank(utoken) || childId <= 0 || StringUtils.isBlank(name)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateChildName(utoken, childId, name)));
    }

    @RequestMapping(value = "/child/sex", method = RequestMethod.POST)
    public ResponseMessage updateChildSex(@RequestParam String utoken,
                                          @RequestParam(value = "cid") long childId,
                                          @RequestParam String sex) {
        if (StringUtils.isBlank(utoken) || childId <= 0 || StringUtils.isBlank(sex)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateChildSex(utoken, childId, sex)));
    }

    @RequestMapping(value = "/child/birthday", method = RequestMethod.POST)
    public ResponseMessage updateChildBirthday(@RequestParam String utoken,
                                               @RequestParam(value = "cid") long childId,
                                               @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken) || childId <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.updateChildBirthday(utoken, childId, birthday)));
    }

    @RequestMapping(value = "/child/delete", method = RequestMethod.POST)
    public ResponseMessage deleteChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if(StringUtils.isBlank(utoken) || childId <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processUser(userServiceApi.USER.deleteChild(utoken, childId)));
    }

    @RequestMapping(value = "/child/list", method = RequestMethod.GET)
    public ResponseMessage listChildren(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(userServiceApi.USER.listChildren(utoken));
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public ResponseMessage listOrders(@RequestParam String utoken,
                                      @RequestParam(defaultValue = "1") int status,
                                      @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || start < 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        return ResponseMessage.SUCCESS(processPagedOrders(dealServiceApi.ORDER.listOrders(user.getId(), status < 0 ? 1 : status, start, Configuration.getInt("PageSize.Order"))));
    }

    @RequestMapping(value = "/order/detail", method = RequestMethod.GET)
    public ResponseMessage getOrderDetail(@RequestParam String utoken,
                                          @RequestParam(value = "oid") long orderId,
                                          @RequestParam(value = "pid") long productId) {
        if (StringUtils.isBlank(utoken) || orderId <= 0 || productId <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        return ResponseMessage.SUCCESS(processOrder(dealServiceApi.ORDER.get(user.getId(), orderId, productId)));
    }

    @RequestMapping(value = "/coupon", method = RequestMethod.GET)
    public ResponseMessage listCoupons(@RequestParam String utoken,
                                       @RequestParam(value = "oid", defaultValue = "0") long orderId,
                                       @RequestParam(defaultValue = "0") int status,
                                       @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || orderId < 0 || status < 0 || start < 0) return ResponseMessage.BAD_REQUEST;

        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder()
                .add("utoken", utoken)
                .add("oid", orderId)
                .add("status", status)
                .add("start", start)
                .add("count", Configuration.getInt("PageSize.Coupon"));
        MomiaHttpRequest request = MomiaHttpRequest.GET(url("coupon/user"), builder.build());

        return executeRequest(request);
    }

    @RequestMapping(value = "/favorite", method = RequestMethod.GET)
    public ResponseMessage getFavoritesOfUser(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || start < 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        PagedProducts favorites = productServiceApi.FAVORITE.listFavorites(user.getId(), start, Configuration.getInt("PageSize.Favorite"));

        return ResponseMessage.SUCCESS(processPagedProducts(favorites));
    }
}
