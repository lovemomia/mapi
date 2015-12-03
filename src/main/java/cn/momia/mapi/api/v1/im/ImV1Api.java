package cn.momia.mapi.api.v1.im;

import cn.momia.api.im.ImServiceApi;
import cn.momia.api.im.dto.ImUser;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/im")
public class ImV1Api extends AbstractV1Api {
    @Autowired private ImServiceApi imServiceApi;

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public MomiaHttpResponse getImToken(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(imServiceApi.getImToken(utoken));
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public MomiaHttpResponse getImUser(@RequestParam(value = "uid") long userId) {
        if (userId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        ImUser imUser = imServiceApi.getImUser(userId);
        imUser.setAvatar(ImageFile.smallUrl(imUser.getAvatar()));

        return MomiaHttpResponse.SUCCESS(imUser);
    }

    @RequestMapping(value = "/group/member", method = RequestMethod.GET)
    public MomiaHttpResponse listGroupMembers(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<ImUser> teachers = new ArrayList<ImUser>();
        List<ImUser> customers = new ArrayList<ImUser>();
        List<ImUser> members = imServiceApi.listGroupMembers(utoken, id);
        for (ImUser member : members) {
            member.setAvatar(ImageFile.smallUrl(member.getAvatar()));
            if (member.isTeacher()) teachers.add(member);
            else customers.add(member);
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("teachers", teachers);
        resultJson.put("customers", customers);

        return MomiaHttpResponse.SUCCESS(resultJson);
    }
}
