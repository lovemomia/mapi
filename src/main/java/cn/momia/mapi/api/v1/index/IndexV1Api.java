package cn.momia.mapi.api.v1.index;

import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.Subject;
import cn.momia.api.event.EventServiceApi;
import cn.momia.api.event.dto.Banner;
import cn.momia.api.event.dto.Event;
import cn.momia.api.event.dto.Icon;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/index")
public class IndexV1Api extends AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexV1Api.class);

    @Autowired private EventServiceApi eventServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;

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
        indexJson.put("subjects", getTrialSubjects(cityId, start));

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    private List<Banner> getBanners(int cityId, int clientType) {
        List<Banner> banners = eventServiceApi.listBanners(cityId, Configuration.getInt("PageSize.Banner"));
        List<Banner> filteredBanners = new ArrayList<Banner>();
        for (Banner banner : banners) {
            if (banner.getPlatform() != 0 && banner.getPlatform() != clientType) continue;
            banner.setCover(ImageFile.url(banner.getCover()));
            banner.setAction(buildAction(banner.getAction(), clientType));
            filteredBanners.add(banner);
        }

        return filteredBanners;
    }

    private List<Icon> getIcons(int cityId, int clientType) {
        List<Icon> icons = eventServiceApi.listIcons(cityId, Configuration.getInt("PageSize.Icon"));
        List<Icon> filteredIcons = new ArrayList<Icon>();
        for (Icon icon : icons) {
            if (icon.getPlatform() != 0 && icon.getPlatform() != clientType) continue;
            icon.setImg(ImageFile.url(icon.getImg()));
            icon.setAction(buildAction(icon.getAction(), clientType));
            filteredIcons.add(icon);
        }

        return filteredIcons;
    }

    private List<Event> getEvents(int cityId, int clientType) {
        List<Event> events = eventServiceApi.listEvents(cityId, Configuration.getInt("PageSize.Event"));
        List<Event> filteredEvents = new ArrayList<Event>();
        for (Event event : events) {
            if (event.getPlatform() != 0 && event.getPlatform() != clientType) continue;
            event.setImg(ImageFile.url(event.getImg()));
            event.setAction(buildAction(event.getAction(), clientType));
            filteredEvents.add(event);
        }

        if (filteredEvents.size() % 2 != 0) {
            return filteredEvents.subList(0, filteredEvents.size() - 1);
        }

        return filteredEvents;
    }

    private PagedList<Subject> getTrialSubjects(int cityId, int start) {
        try {
            PagedList<Subject> subjects = subjectServiceApi.listTrial(cityId, start, Configuration.getInt("PageSize.Trial"));
            for (Subject subject : subjects.getList()) {
                subject.setCover(ImageFile.largeUrl(subject.getCover()));
            }

            return subjects;
        } catch (Exception e) {
            LOGGER.error("fail to list trial subjects", e);
            return PagedList.EMPTY;
        }
    }
}
