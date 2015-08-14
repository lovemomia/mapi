package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.service.product.api.ProductServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/topic")
public class TopicV1Api extends AbstractV1Api {
    @Autowired private ProductServiceApi productServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage topic(@RequestParam long id) {
        if(id <= 0) return ResponseMessage.BAD_REQUEST;

        return ResponseMessage.SUCCESS(processTopic(productServiceApi.TOPIC.get(id)));
    }
}
