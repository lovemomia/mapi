package cn.momia.mapi.api.v1.im;

import cn.momia.api.im.ImServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/im")
public class ImV1Api {
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public MomiaHttpResponse token(@RequestParam String utoken) {
        return MomiaHttpResponse.SUCCESS(ImServiceApi.IM.getToken(utoken));
    }
}
