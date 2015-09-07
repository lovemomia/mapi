package cn.momia.mapi.api.v1;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.common.CommonServiceApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/region")
public class RegionV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getAllRegions() {
        return MomiaHttpResponse.SUCCESS(CommonServiceApi.REGION.getAll());
    }

    @RequestMapping(value = "/district/tree", method = RequestMethod.GET)
    public MomiaHttpResponse getDistrictTree() {
        return MomiaHttpResponse.SUCCESS(CommonServiceApi.REGION.getCityDistrictTree());
    }
}
