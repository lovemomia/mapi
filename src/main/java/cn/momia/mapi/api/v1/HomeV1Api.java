package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.common.http.MomiaHttpParamBuilder;
import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.common.http.MomiaHttpResponseCollector;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.mapi.api.v1.dto.base.Dto;
import cn.momia.mapi.api.v1.dto.home.HomeDto;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/home")
public class HomeV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage home(@RequestParam(value = "pageindex") final int pageIndex,
                                @RequestParam(value = "city") int cityId) {
        if (pageIndex < 0 || cityId < 0) return ResponseMessage.SUCCESS(HomeDto.EMPTY);

        int pageSize = Configuration.getInt("PageSize.Product");
        final int start = pageIndex * pageSize;
        final int count = pageSize;
        List<MomiaHttpRequest> requests = buildHomeRequests(cityId, start, count);

        return executeRequests(requests, new Function<MomiaHttpResponseCollector, Object>() {
            @Override
            public Object apply(MomiaHttpResponseCollector collector) {
                return buildHomeDto(collector, start, count, pageIndex);
            }
        });
    }

    private List<MomiaHttpRequest> buildHomeRequests(int cityId, int start, int count) {
        List<MomiaHttpRequest> requests = new ArrayList<MomiaHttpRequest>();
        if (start == 0) requests.add(buildBannersRequest(cityId));
        requests.add(buildProductsRequest(cityId, start, count));

        return requests;
    }

    private MomiaHttpRequest buildBannersRequest(int cityId) {
        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder()
                .add("city", cityId)
                .add("count", Configuration.getInt("PageSize.Banner"));

        return MomiaHttpRequest.GET("banners", true, url("banner"), builder.build());
    }

    private MomiaHttpRequest buildProductsRequest(int cityId, int start, int count) {
        MomiaHttpParamBuilder builder = new MomiaHttpParamBuilder()
                .add("city", cityId)
                .add("start", start)
                .add("count", count);

        return MomiaHttpRequest.GET("products", true, url("product"), builder.build());
    }

    private Dto buildHomeDto(MomiaHttpResponseCollector collector, int start, int count, int pageIndex) {
        HomeDto homeDto = new HomeDto();

        if (pageIndex == 0) homeDto.setBanners((JSONArray) collector.getResponse("banners"));

        JSONObject productsPackJson = (JSONObject) pagedProductsFunc.apply(collector.getResponse("products"));
        homeDto.setProducts(productsPackJson.getJSONArray("list"));

        long totalCount = productsPackJson.getLong("totalCount");
        if (start + count < totalCount) homeDto.setNextpage(pageIndex + 1);

        return homeDto;
    }
}
