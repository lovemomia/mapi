package cn.momia.mapi.api.v1.base;

import cn.momia.api.base.BaseServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/recommend")
public class RecommendV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.POST)
    public MomiaHttpResponse recommend(@RequestParam String content,
                                       @RequestParam String time,
                                       @RequestParam String address,
                                       @RequestParam String contacts) {
        if (StringUtils.isBlank(content)) return MomiaHttpResponse.FAILED("爆料内容不能为空");
        if (StringUtils.isBlank(time)) return MomiaHttpResponse.FAILED("时间不能为空");
        if (StringUtils.isBlank(address)) return MomiaHttpResponse.FAILED("地址不能为空");
        if (StringUtils.isBlank(contacts)) return MomiaHttpResponse.FAILED("联系方式不能为空");

        if (content.length() > 600) return MomiaHttpResponse.FAILED("爆料字数超出限制");

        if (!BaseServiceApi.RECOMMEND.addRecommend(content, time, address, contacts)) return MomiaHttpResponse.FAILED("提交爆料信息失败");
        return MomiaHttpResponse.SUCCESS;
    }
}
