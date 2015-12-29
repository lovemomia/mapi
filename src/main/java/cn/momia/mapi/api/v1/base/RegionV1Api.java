package cn.momia.mapi.api.v1.base;

import cn.momia.api.poi.PoiServiceApi;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/region")
public class RegionV1Api extends AbstractApi {
    @Autowired private PoiServiceApi poiServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse listAll() {
        return MomiaHttpResponse.SUCCESS(poiServiceApi.listAllRegions());
    }
}
