package cn.momia.mapi.api.v1.im;

import cn.momia.api.im.ImServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/im")
public class ImV1Api extends AbstractV1Api {
    @Autowired private ImServiceApi imServiceApi;

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public MomiaHttpResponse getImToken(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(imServiceApi.getImToken(utoken));
    }
}
