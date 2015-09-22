package cn.momia.mapi.api.v1;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.ParticipantDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/participant")
public class ParticipantV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse add(@RequestParam String utoken, @RequestParam String participant) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(participant)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject paticipantJson = JSON.parseObject(participant);
        paticipantJson.put("userId", UserServiceApi.USER.get(utoken).getId());
        UserServiceApi.PARTICIPANT.add(JSON.toJavaObject(paticipantJson, ParticipantDto.class));

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(UserServiceApi.PARTICIPANT.get(utoken, id));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public MomiaHttpResponse update(@RequestParam String utoken, @RequestParam String participant) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(participant)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject paticipantJson = JSON.parseObject(participant);
        paticipantJson.put("userId", UserServiceApi.USER.get(utoken).getId());
        UserServiceApi.PARTICIPANT.update(JSON.toJavaObject(paticipantJson, ParticipantDto.class));

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse delete(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserServiceApi.PARTICIPANT.delete(utoken, id);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public MomiaHttpResponse list(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(UserServiceApi.PARTICIPANT.list(utoken));
    }
}
