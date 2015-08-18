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
@RequestMapping("/v1/feedback")
public class FeedbackV1Api extends AbstractV1Api {
    @Autowired private CommonServiceApi commonServiceApi;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseMessage addFeedback(@RequestParam String content, @RequestParam String email) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(email)) return ResponseMessage.BAD_REQUEST;

        commonServiceApi.FEEDBACK.addFeedback(content, email);

        return ResponseMessage.SUCCESS;
    }
}
