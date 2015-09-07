package cn.momia.mapi.api.v1;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.common.CommonServiceApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/feedback")
public class FeedbackV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse addFeedback(@RequestParam String content, @RequestParam String email) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(email)) return MomiaHttpResponse.BAD_REQUEST;

        CommonServiceApi.FEEDBACK.addFeedback(content, email);

        return MomiaHttpResponse.SUCCESS;
    }
}
