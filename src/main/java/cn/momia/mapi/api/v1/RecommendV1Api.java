package cn.momia.mapi.api.v1;

import cn.momia.api.base.BaseServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/recommend")
public class RecommendV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse recommend(@RequestParam String content,
                                       @RequestParam String time,
                                       @RequestParam String address,
                                       @RequestParam String contacts) {
        if (StringUtils.isBlank(content) ||
                StringUtils.isBlank(time) ||
                StringUtils.isBlank(address) ||
                StringUtils.isBlank(contacts)) return MomiaHttpResponse.BAD_REQUEST;

        BaseServiceApi.RECOMMEND.addRecommend(content, time, address, contacts);

        return MomiaHttpResponse.SUCCESS;
    }
}
