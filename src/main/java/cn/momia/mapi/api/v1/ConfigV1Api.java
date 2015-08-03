package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.web.response.ResponseMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/config")
public class ConfigV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage getConfig() {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("showLeader", Configuration.getBoolean("App.ShowLeader"));

        return ResponseMessage.SUCCESS(config);
    }
}
