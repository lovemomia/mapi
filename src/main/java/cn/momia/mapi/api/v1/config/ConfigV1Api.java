package cn.momia.mapi.api.v1.config;

import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/config")
public class ConfigV1Api extends AbstractApi {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getConfig() {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("ShareCoupon", Configuration.getBoolean("AppConf.ShareCoupon"));

        return MomiaHttpResponse.SUCCESS(config);
    }
}
