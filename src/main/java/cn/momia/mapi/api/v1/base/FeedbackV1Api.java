package cn.momia.mapi.api.v1.base;

import cn.momia.api.base.FeedbackServiceApi;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/feedback")
public class FeedbackV1Api extends AbstractApi {
    @Autowired private FeedbackServiceApi feedbackServiceApi;

    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse addFeedback(@RequestParam String content, @RequestParam String contact) {
        if (StringUtils.isBlank(content)) return MomiaHttpResponse.FAILED("反馈内容不能为空");
        if (StringUtils.isBlank(contact)) return MomiaHttpResponse.FAILED("联系方式不能为空");

        if (content.length() > 480) return MomiaHttpResponse.FAILED("反馈内容字数超出限制");

        if (!feedbackServiceApi.add(content, contact)) return MomiaHttpResponse.FAILED("提交反馈意见失败");
        return MomiaHttpResponse.SUCCESS;
    }
}
