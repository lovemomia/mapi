package cn.momia.mapi.api.v1;

import cn.momia.api.deal.entity.Coupon;
import cn.momia.api.product.entity.Product;
import cn.momia.common.api.entity.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.deal.DealServiceApi;
import cn.momia.api.deal.entity.Order;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.entity.Participant;
import cn.momia.api.user.entity.User;
import cn.momia.common.webapp.config.Configuration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
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
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getUser(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.get(utoken)));
    }

    @RequestMapping(value = "/nickname", method = RequestMethod.POST)
    public MomiaHttpResponse updateNickName(@RequestParam String utoken, @RequestParam(value = "nickname") String nickName) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(nickName)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateNickName(utoken, nickName)));
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public MomiaHttpResponse updateAvatar(@RequestParam String utoken, @RequestParam String avatar) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(avatar)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateAvatar(utoken, avatar)));
    }

    @RequestMapping(value = "/name", method = RequestMethod.POST)
    public MomiaHttpResponse updateName(@RequestParam String utoken, @RequestParam String name) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(name)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateName(utoken, name)));
    }

    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    public MomiaHttpResponse updateSex(@RequestParam String utoken, @RequestParam String sex) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(sex)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateSex(utoken, sex)));
    }

    @RequestMapping(value = "/birthday", method = RequestMethod.POST)
    public MomiaHttpResponse updateBirthday(@RequestParam String utoken, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateBirthday(utoken, birthday)));
    }

    @RequestMapping(value = "/city", method = RequestMethod.POST)
    public MomiaHttpResponse updateCity(@RequestParam String utoken, @RequestParam(value = "city") int cityId) {
        if (StringUtils.isBlank(utoken) || cityId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateCity(utoken, cityId)));
    }

    @RequestMapping(value = "/region", method = RequestMethod.POST)
    public MomiaHttpResponse updateRegion(@RequestParam String utoken, @RequestParam(value = "region") int regionId) {
        if (StringUtils.isBlank(utoken) || regionId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateRegion(utoken, regionId)));
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    public MomiaHttpResponse updateAddress(@RequestParam String utoken, @RequestParam String address) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(address)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateAddress(utoken, address)));
    }

    @RequestMapping(value = "/child", method = RequestMethod.POST)
    public MomiaHttpResponse addChild(@RequestParam String utoken, @RequestParam String children) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(children)) return MomiaHttpResponse.BAD_REQUEST;

        long userId = UserServiceApi.USER.get(utoken).getId();
        List<Participant> participants = new ArrayList<Participant>();
        JSONArray childrenJson = JSONArray.parseArray(children);
        for (int i = 0; i < childrenJson.size(); i++) {
            Participant participant = JSON.toJavaObject(childrenJson.getJSONObject(i), Participant.class);
            participant.setUserId(userId);
            participants.add(participant);
        }

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.addChildren(participants)));
    }

    @RequestMapping(value = "/child", method = RequestMethod.GET)
    public MomiaHttpResponse getChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if (StringUtils.isBlank(utoken) || childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(UserServiceApi.USER.getChild(utoken, childId));
    }

    @RequestMapping(value = "/child/name", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildName(@RequestParam String utoken,
                                             @RequestParam(value = "cid") long childId,
                                             @RequestParam String name) {
        if (StringUtils.isBlank(utoken) || childId <= 0 || StringUtils.isBlank(name))
            return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateChildName(utoken, childId, name)));
    }

    @RequestMapping(value = "/child/sex", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildSex(@RequestParam String utoken,
                                            @RequestParam(value = "cid") long childId,
                                            @RequestParam String sex) {
        if (StringUtils.isBlank(utoken) || childId <= 0 || StringUtils.isBlank(sex)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateChildSex(utoken, childId, sex)));
    }

    @RequestMapping(value = "/child/birthday", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildBirthday(@RequestParam String utoken,
                                                 @RequestParam(value = "cid") long childId,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken) || childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.updateChildBirthday(utoken, childId, birthday)));
    }

    @RequestMapping(value = "/child/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if (StringUtils.isBlank(utoken) || childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(UserServiceApi.USER.deleteChild(utoken, childId)));
    }

    @RequestMapping(value = "/child/list", method = RequestMethod.GET)
    public MomiaHttpResponse listChildren(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(UserServiceApi.USER.listChildren(utoken));
    }

    @RequestMapping(value = "/code", method = RequestMethod.GET)
    public MomiaHttpResponse getInviteCode(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(UserServiceApi.USER.getInviteCode(utoken));
    }


    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public MomiaHttpResponse listOrders(@RequestParam String utoken,
                                        @RequestParam(defaultValue = "1") int status,
                                        @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<Order> orders = processPagedOrders(DealServiceApi.ORDER.listOrders(utoken, status < 0 ? 1 : status, start, Configuration.getInt("PageSize.Order")));

        return MomiaHttpResponse.SUCCESS(orders);
    }

    @RequestMapping(value = "/order/detail", method = RequestMethod.GET)
    public MomiaHttpResponse getOrderDetail(@RequestParam String utoken,
                                            @RequestParam(value = "oid") long orderId,
                                            @RequestParam(value = "pid") long productId) {
        if (StringUtils.isBlank(utoken) || orderId <= 0 || productId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Order order = processOrder(DealServiceApi.ORDER.get(utoken, orderId, productId));

        return MomiaHttpResponse.SUCCESS(order);
    }

    @RequestMapping(value = "/coupon", method = RequestMethod.GET)
    public MomiaHttpResponse listCoupons(@RequestParam String utoken,
                                         @RequestParam(value = "oid", defaultValue = "0") long orderId,
                                         @RequestParam(defaultValue = "0") int status,
                                         @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || orderId < 0 || status < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<Coupon> coupons = DealServiceApi.COUPON.listCoupons(utoken, orderId, status, start, Configuration.getInt("PageSize.Coupon"));

        return MomiaHttpResponse.SUCCESS(coupons);
    }

    @RequestMapping(value = "/favorite", method = RequestMethod.GET)
    public MomiaHttpResponse getFavoritesOfUser(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = UserServiceApi.USER.get(utoken);
        PagedList<Product> favorites = ProductServiceApi.FAVORITE.listFavorites(user.getId(), start, Configuration.getInt("PageSize.Favorite"));

        return MomiaHttpResponse.SUCCESS(processPagedProducts(favorites, IMAGE_MIDDLE));
    }
}
