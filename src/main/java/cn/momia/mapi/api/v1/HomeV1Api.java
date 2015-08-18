package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.product.PagedProducts;
import cn.momia.api.product.topic.Banner;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/home")
public class HomeV1Api extends AbstractV1Api {
    @Autowired private ProductServiceApi productServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage home(@RequestParam(value = "city") int cityId, @RequestParam(value = "pageindex") final int pageIndex) {
        if (cityId < 0 || pageIndex < 0) return ResponseMessage.BAD_REQUEST;

        int pageSize = Configuration.getInt("PageSize.Product");
        final int start = pageIndex * pageSize;
        final int count = pageSize;

        List<Banner> banners = null;
        if (start== 0) banners = productServiceApi.TOPIC.listBanners(cityId, Configuration.getInt("PageSize.Banner"));
        PagedProducts products = productServiceApi.PRODUCT.list(cityId, start, count);

        return ResponseMessage.SUCCESS(buildHomeResponse(banners, products, start, count, pageIndex));
    }

    private JSONObject buildHomeResponse(List<Banner> banners, PagedProducts products, int start, int count, int pageIndex) {
        JSONObject homeJson = new JSONObject();

        if (pageIndex == 0) homeJson.put("banners", processBanners(banners));

        products = processPagedProducts(products);
        homeJson.put("products", products.getList());

        long totalCount = products.getTotalCount();
        if (start + count < totalCount) homeJson.put("nextpage", pageIndex + 1);

        return homeJson;
    }
}
