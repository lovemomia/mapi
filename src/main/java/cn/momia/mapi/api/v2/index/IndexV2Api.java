package cn.momia.mapi.api.v2.index;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.event.EventServiceApi;
import cn.momia.api.event.dto.BannerDto;
import cn.momia.api.event.dto.EventDto;
import cn.momia.api.event.dto.IconDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v2.AbstractV2Api;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/v2/index")
public class IndexV2Api extends AbstractV2Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexV2Api.class);

    @Autowired private EventServiceApi eventServiceApi;
    @Autowired private CourseServiceApi courseServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse index(@RequestParam(value = "city") int cityId,
                                   @RequestParam int start,
                                   HttpServletRequest request) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject indexJson = new JSONObject();
        if (start == 0) {
            int clientType = getClientType(request);

            indexJson.put("banners", getBanners(cityId, clientType));
            indexJson.put("icons", getIcons(cityId, clientType));
            indexJson.put("events", getEvents(cityId, clientType));
        }
        indexJson.put("courses", getRecommendCourses(cityId, start));

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    private List<BannerDto> getBanners(int cityId, int clientType) {
        List<BannerDto> banners = eventServiceApi.listBanners(cityId, Configuration.getInt("PageSize.Banner"));
        for (BannerDto banner : banners) {
            banner.setCover(ImageFile.url(banner.getCover()));
            banner.setAction(buildAction(banner.getAction(), clientType));
        }

        return banners;
    }

    private List<IconDto> getIcons(int cityId, int clientType) {
        List<IconDto> icons = eventServiceApi.listIcons(cityId, Configuration.getInt("PageSize.Icon"));
        for (IconDto icon : icons) {
            icon.setImg(ImageFile.url(icon.getImg()));
            icon.setAction(buildAction(icon.getAction(), clientType));
        }

        return icons;
    }

    private List<EventDto> getEvents(int cityId, int clientType) {
        List<EventDto> events = eventServiceApi.listEvents(cityId, Configuration.getInt("PageSize.Event"));
        for (EventDto event : events) {
            event.setImg(ImageFile.url(event.getImg()));
            event.setAction(buildAction(event.getAction(), clientType));
        }

        return events;
    }

    private PagedList<CourseDto> getRecommendCourses(int cityId, int start) {
        try {
            PagedList<CourseDto> courses = courseServiceApi.listRecommend(cityId, start, Configuration.getInt("PageSize.Course"));
            for (CourseDto course : courses.getList()) {
                course.setCover(ImageFile.largeUrl(course.getCover()));
            }

            return courses;
        } catch (Exception e) {
            LOGGER.error("fail to list recommend courses", e);
            return PagedList.EMPTY;
        }
    }
}