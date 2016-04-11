package cn.momia.mapi.api.feed;

import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/feed")
public class FeedV1Api extends AbstractApi {
    @Deprecated
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse list() {
        return MomiaHttpResponse.SUCCESS(PagedList.EMPTY);
    }
}
