package cn.momia.mapi.common.util;

import cn.momia.api.common.CommonServiceApi;
import cn.momia.api.common.city.City;
import cn.momia.api.common.region.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MetaUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaUtil.class);

    private static Map<Integer, City> citiesMap = new HashMap<Integer, City>();
    private static Map<Integer, Region> regionsMap = new HashMap<Integer, Region>();

    public void init() {
        try {
            citiesMap = new HashMap<Integer, City>();
            for (City city : CommonServiceApi.CITY.getAll()) citiesMap.put(city.getId(), city);

            regionsMap = new HashMap<Integer, Region>();
            for (Region region : CommonServiceApi.REGION.getAll()) regionsMap.put(region.getId(), region);
        } catch (Exception e) {
            LOGGER.error("fail to init meta util");
        }
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
