package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.service.common.api.CommonServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/city")
public class CityV1Api extends AbstractV1Api {
    @Autowired CommonServiceApi commonServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage getAllCities() {
        return ResponseMessage.SUCCESS(commonServiceApi.CITY.getAll());
    }
}
