package cn.momia.mapi.api.v1;

import cn.momia.api.base.BaseServiceApi;
import cn.momia.common.api.http.MomiaHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/city")
public class CityV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getAllCities() {
        return MomiaHttpResponse.SUCCESS(BaseServiceApi.CITY.getAll());
    }
}
