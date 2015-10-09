package cn.momia.mapi.api.v1.home;

import cn.momia.api.event.EventServiceApi;
import cn.momia.api.event.dto.BannerDto;
import cn.momia.api.product.dto.ProductDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
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

        List<BannerDto> banners = null;
        if (start== 0) banners = EventServiceApi.EVENT.listBanners(cityId, Configuration.getInt("PageSize.Banner"));
        PagedList<ProductDto> products = ProductServiceApi.PRODUCT.list(cityId, start, count);

        return MomiaHttpResponse.SUCCESS(buildHomeResponse(banners, products, pageIndex, getClientType(request)));
    }

    private JSONObject buildHomeResponse(List<BannerDto> banners, PagedList<ProductDto> products, int pageIndex, int clientType) {
        JSONObject homeJson = new JSONObject();

        if (pageIndex == 0) homeJson.put("banners", processBanners(banners, clientType));

        products = processPagedProducts(products, ImageFile.Size.LARGE);
        homeJson.put("products", products.getList());

        Integer nextIndex = products.getNextIndex();
        if (nextIndex != null && nextIndex > 0) homeJson.put("nextpage", pageIndex + 1);

        return homeJson;
    }

    private List<BannerDto> processBanners(List<BannerDto> banners, int clientType) {
        for (BannerDto banner : banners) {
            banner.setCover(ImageFile.url(banner.getCover()));
            banner.setAction(buildLink(banner.getAction(), clientType));
        }

        return banners;
    }
}
