package cn.momia.mapi.api.v1;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.product.entity.PagedProducts;
import cn.momia.api.product.entity.Banner;
import cn.momia.common.webapp.config.Configuration;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/v1/home")
public class HomeV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse home(@RequestParam(value = "city") int cityId,
                                  @RequestParam(value = "pageindex") final int pageIndex,
                                  HttpServletRequest request) {
        if (cityId < 0 || pageIndex < 0) return MomiaHttpResponse.BAD_REQUEST;

        int pageSize = Configuration.getInt("PageSize.Product");
        final int start = pageIndex * pageSize;
        final int count = pageSize;

        List<Banner> banners = null;
        if (start== 0) banners = ProductServiceApi.TOPIC.listBanners(cityId, Configuration.getInt("PageSize.Banner"));
        PagedProducts products = ProductServiceApi.PRODUCT.list(cityId, start, count);

        return MomiaHttpResponse.SUCCESS(buildHomeResponse(banners, products, start, count, pageIndex, getClientType(request)));
    }

    private JSONObject buildHomeResponse(List<Banner> banners, PagedProducts products, int start, int count, int pageIndex, int clientType) {
        JSONObject homeJson = new JSONObject();

        if (pageIndex == 0) homeJson.put("banners", processBanners(banners, clientType));

        products = processPagedProducts(products);
        homeJson.put("products", products.getList());

        long totalCount = products.getTotalCount();
        if (start + count < totalCount) homeJson.put("nextpage", pageIndex + 1);

        return homeJson;
    }
}
