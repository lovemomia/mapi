package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.common.CommonServiceApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/recommend")
public class RecommendV1Api extends AbstractV1Api {
    @Autowired CommonServiceApi commonServiceApi;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseMessage recommend(@RequestParam String content,
                                     @RequestParam String time,
                                     @RequestParam String address,
                                     @RequestParam String contacts) {
        if (StringUtils.isBlank(content) ||
                StringUtils.isBlank(time) ||
                StringUtils.isBlank(address) ||
                StringUtils.isBlank(contacts)) return ResponseMessage.BAD_REQUEST;

        commonServiceApi.RECOMMEND.addRecommend(content, time, address, contacts);

        return ResponseMessage.SUCCESS;
    }
}
