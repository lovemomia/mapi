package cn.momia.mapi.api.v1;

import cn.momia.api.base.http.MomiaHttpResponse;
import cn.momia.api.product.ProductServiceApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/topic")
public class TopicV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse topic(@RequestParam long id) {
        if(id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(processTopic(ProductServiceApi.TOPIC.get(id)));
    }
}
