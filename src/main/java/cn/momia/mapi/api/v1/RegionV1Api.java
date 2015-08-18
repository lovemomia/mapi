package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.api.common.CommonServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/region")
public class RegionV1Api extends AbstractV1Api {
    @Autowired private CommonServiceApi commonServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage getAllRegions() {
        return ResponseMessage.SUCCESS(commonServiceApi.REGION.getAll());
    }

    @RequestMapping(value = "/district/tree", method = RequestMethod.GET)
    public ResponseMessage getDistrictTree() {
        return ResponseMessage.SUCCESS(commonServiceApi.REGION.getCityDistrictTree());
    }
}
