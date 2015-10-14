package cn.momia.mapi.api.v1.user;

import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.OrderDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.util.SexUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/v1/user")
public class UserV1Api extends AbstractV1Api {
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getUser(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.get(utoken)));
    }

    @RequestMapping(value = "/nickname", method = RequestMethod.POST)
    public MomiaHttpResponse updateNickName(@RequestParam String utoken, @RequestParam(value = "nickname") String nickName) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(nickName)) return MomiaHttpResponse.FAILED("用户昵称不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateNickName(utoken, nickName)));
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public MomiaHttpResponse updateAvatar(@RequestParam String utoken, @RequestParam String avatar) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(avatar)) return MomiaHttpResponse.FAILED("用户头像不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateAvatar(utoken, avatar)));
    }

    @RequestMapping(value = "/name", method = RequestMethod.POST)
    public MomiaHttpResponse updateName(@RequestParam String utoken, @RequestParam String name) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(name)) return MomiaHttpResponse.FAILED("用户名字不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateName(utoken, name)));
    }

    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    public MomiaHttpResponse updateSex(@RequestParam String utoken, @RequestParam String sex) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(sex) || SexUtil.isInvalid(sex)) return MomiaHttpResponse.FAILED("无效的用户性别");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateSex(utoken, sex)));
    }

    @RequestMapping(value = "/birthday", method = RequestMethod.POST)
    public MomiaHttpResponse updateBirthday(@RequestParam String utoken, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (birthday == null) return MomiaHttpResponse.FAILED("出生日期不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateBirthday(utoken, birthday)));
    }

    @RequestMapping(value = "/city", method = RequestMethod.POST)
    public MomiaHttpResponse updateCity(@RequestParam String utoken, @RequestParam(value = "city") int cityId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (cityId <= 0) return MomiaHttpResponse.FAILED("无效的城市ID");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateCity(utoken, cityId)));
    }

    @RequestMapping(value = "/region", method = RequestMethod.POST)
    public MomiaHttpResponse updateRegion(@RequestParam String utoken, @RequestParam(value = "region") int regionId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (regionId <= 0) return MomiaHttpResponse.FAILED("无效的区域ID");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateRegion(utoken, regionId)));
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    public MomiaHttpResponse updateAddress(@RequestParam String utoken, @RequestParam String address) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(address)) return MomiaHttpResponse.FAILED("地址不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateAddress(utoken, address)));
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public MomiaHttpResponse listOrders(@RequestParam String utoken, @RequestParam int status, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (status <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<OrderDto> orders = processPagedOrders(subjectServiceApi.listOrders(utoken, status, start, Configuration.getInt("PageSize.Order")));
        return MomiaHttpResponse.SUCCESS(orders);
    }
}
