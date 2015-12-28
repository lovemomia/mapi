package cn.momia.mapi.api.v1.base;

import cn.momia.api.base.MetaServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/city")
public class CityV1Api extends AbstractApi {
    @Autowired private MetaServiceApi metaServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse listAll() {
        return MomiaHttpResponse.SUCCESS(metaServiceApi.listAllCities());
    }
}
