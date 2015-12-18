package cn.momia.mapi.api;

import cn.momia.api.event.EventServiceApi;
import cn.momia.api.event.dto.Banner;
import cn.momia.api.event.dto.Event;
import cn.momia.api.event.dto.Icon;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class AbstractIndexApi extends AbstractApi {
    @Autowired
    private EventServiceApi eventServiceApi;

    protected List<Banner> getBanners(int cityId, int clientType) {
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

    protected List<Icon> getIcons(int cityId, int clientType) {
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

    protected List<Event> getEvents(int cityId, int clientType) {
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
}
