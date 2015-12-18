package cn.momia.mapi.api.v1.user;

import cn.momia.api.user.ChildServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.util.SexUtil;
import cn.momia.mapi.api.AbstractApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/v1/user/child")
public class ChildV1Api extends AbstractApi {
    @Autowired private ChildServiceApi childServiceApi;

    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse addChild(@RequestParam String utoken, @RequestParam String children) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(children)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(completeUserImgs(childServiceApi.add(utoken, children)));
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(completeChildImg(childServiceApi.get(utoken, childId)));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public MomiaHttpResponse listChildren(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(completeChildrenImgs(childServiceApi.list(utoken)));
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildAvatar(@RequestParam String utoken,
                                               @RequestParam(value = "cid") long childId,
                                               @RequestParam String avatar) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (StringUtils.isBlank(avatar)) return MomiaHttpResponse.FAILED("孩子头像不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(childServiceApi.updateAvatar(utoken, childId, avatar)));
    }

    @RequestMapping(value = "/name", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildName(@RequestParam String utoken,
                                             @RequestParam(value = "cid") long childId,
                                             @RequestParam String name) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (StringUtils.isBlank(name)) return MomiaHttpResponse.FAILED("孩子姓名不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(childServiceApi.updateName(utoken, childId, name)));
    }

    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildSex(@RequestParam String utoken,
                                            @RequestParam(value = "cid") long childId,
                                            @RequestParam String sex) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (SexUtil.isInvalid(sex)) return MomiaHttpResponse.FAILED("无效的孩子性别");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(childServiceApi.updateSex(utoken, childId, sex)));
    }

    @RequestMapping(value = "/birthday", method = RequestMethod.POST)
    public MomiaHttpResponse updateChildBirthday(@RequestParam String utoken,
                                                 @RequestParam(value = "cid") long childId,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (birthday == null) return MomiaHttpResponse.FAILED("无效的孩子生日");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(childServiceApi.updateBirthday(utoken, childId, birthday)));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteChild(@RequestParam String utoken, @RequestParam(value = "cid") long childId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(completeUserImgs(childServiceApi.delete(utoken, childId)));
    }
}
