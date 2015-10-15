package cn.momia.mapi.api.v1.user;

import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.UserChildDto;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.util.SexUtil;
import cn.momia.mapi.api.v1.AbstractV1Api;
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
@RequestMapping("/v1/user/child")
public class ChildV1Api extends AbstractV1Api {
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse addChild(@RequestParam String utoken, @RequestParam String children) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(children)) return MomiaHttpResponse.BAD_REQUEST;

        long userId = userServiceApi.get(utoken).getId();
        List<UserChildDto> childDtos = new ArrayList<UserChildDto>();
        JSONArray childrenJson = JSONArray.parseArray(children);
        for (int i = 0; i < childrenJson.size(); i++) {
            UserChildDto childDto = JSON.toJavaObject(childrenJson.getJSONObject(i), UserChildDto.class);
            childDto.setUserId(userId);
            childDtos.add(childDto);
        }

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.addChildren(childDtos)));
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processChild(userServiceApi.getChild(utoken, childId)));
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildAvatar(@RequestParam String utoken,
                                               @RequestParam(value = "cid") long childId,
                                               @RequestParam String avatar) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (StringUtils.isBlank(avatar)) return MomiaHttpResponse.FAILED("孩子头像不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateChildAvatar(utoken, childId, avatar)));
    }

    @RequestMapping(value = "/name", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildName(@RequestParam String utoken,
                                             @RequestParam(value = "cid") long childId,
                                             @RequestParam String name) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (StringUtils.isBlank(name)) return MomiaHttpResponse.FAILED("孩子姓名不能为空");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateChildName(utoken, childId, name)));
    }

    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildSex(@RequestParam String utoken,
                                            @RequestParam(value = "cid") long childId,
                                            @RequestParam String sex) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (SexUtil.isInvalid(sex)) return MomiaHttpResponse.FAILED("无效的孩子性别");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateChildSex(utoken, childId, sex)));
    }

    @RequestMapping(value = "/birthday", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildBirthday(@RequestParam String utoken,
                                                 @RequestParam(value = "cid") long childId,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (birthday == null) return MomiaHttpResponse.FAILED("无效的孩子生日");

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.updateChildBirthday(utoken, childId, birthday)));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processUser(userServiceApi.deleteChild(utoken, childId)));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public MomiaHttpResponse listChildren(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(processChildren(userServiceApi.listChildren(utoken)));
    }
}
