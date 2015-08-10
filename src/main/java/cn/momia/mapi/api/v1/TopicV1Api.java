package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.web.response.ResponseMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/topic")
public class TopicV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage topic(@RequestParam long id) {
        if(id <= 0) return ResponseMessage.BAD_REQUEST;

        return executeRequest(MomiaHttpRequest.GET(url("topic", id)), topicFunc);
    }
}
