package cn.momia.mapi.api.v1;

import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.service.common.api.CommonServiceApi;
import cn.momia.service.common.api.region.CityDistrict;
import cn.momia.service.common.api.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/region")
public class RegionV1Api extends AbstractV1Api {
    @Autowired CommonServiceApi commonServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage getAllRegions() {
        List<Region> regions = commonServiceApi.REGION.getAll();

        return ResponseMessage.SUCCESS(regions);
    }

    @RequestMapping(value = "/district/tree", method = RequestMethod.GET)
    public ResponseMessage getDistrictTree() {
        List<CityDistrict> cityDistricts = commonServiceApi.REGION.getCityDistrictTree();

        return ResponseMessage.SUCCESS(cityDistricts);
    }
}
