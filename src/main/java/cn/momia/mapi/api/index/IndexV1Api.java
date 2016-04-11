package cn.momia.mapi.api.index;

import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/index")
public class IndexV1Api extends AbstractIndexApi {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse index(HttpServletRequest request,
                                   @RequestParam(value = "city") int cityId,
                                   @RequestParam int start) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject indexJson = new JSONObject();
        if (start == 0) {
            int platform = getPlatform(request);
            String version = getVersion(request);

            indexJson.put("banners", getBanners(cityId, platform, version));
            indexJson.put("icons", getIcons(cityId, platform, version));
            indexJson.put("events", getEvents(cityId, platform, version, 0));
        }
        indexJson.put("subjects", PagedList.EMPTY);

        return MomiaHttpResponse.SUCCESS(indexJson);
    }
}
