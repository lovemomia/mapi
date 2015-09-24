package cn.momia.mapi.api.v1.product;

import cn.momia.api.product.dto.TopicDto;
import cn.momia.api.product.dto.TopicGroupDto;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
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

    private TopicDto processTopic(TopicDto topic) {
        topic.setCover(ImageFile.url(topic.getCover()));
        for (TopicGroupDto topicGroup : topic.getGroups()) {
            processProducts(topicGroup.getProducts());
        }

        return topic;
    }
}
