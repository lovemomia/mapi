package cn.momia.mapi.common.util;

import cn.momia.service.common.api.CommonServiceApi;
import cn.momia.service.common.api.city.City;
import cn.momia.service.common.api.region.Region;

import java.util.HashMap;
import java.util.Map;

public class MetaUtil {
    private static Map<Integer, City> citiesMap;
    private static Map<Integer, Region> regionsMap;

    private CommonServiceApi commonServiceApi;

    public void setCommonServiceApi(CommonServiceApi commonServiceApi) {
        this.commonServiceApi = commonServiceApi;
    }

    public void init() {
        citiesMap = new HashMap<Integer, City>();
        for (City city : commonServiceApi.CITY.getAll()) citiesMap.put(city.getId(), city);

        regionsMap = new HashMap<Integer, Region>();
        for (Region region : commonServiceApi.REGION.getAll()) regionsMap.put(region.getId(), region);
    }

    public static String getCityName(int cityId) {
        City city = citiesMap.get(cityId);
        return city == null ? "" : city.getName();
    }

    public static String getRegionName(int regionId) {
        Region region = regionsMap.get(regionId);
        return region == null ? "" : region.getName();
    }
}
