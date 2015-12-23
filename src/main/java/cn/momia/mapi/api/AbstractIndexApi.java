package cn.momia.mapi.api;

import cn.momia.api.event.EventServiceApi;
import cn.momia.api.event.dto.Banner;
import cn.momia.api.event.dto.Event;
import cn.momia.api.event.dto.Icon;
import cn.momia.common.client.ClientType;
import cn.momia.common.webapp.config.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class AbstractIndexApi extends AbstractApi {
    @Autowired
    private EventServiceApi eventServiceApi;

    protected List<Banner> getBanners(int cityId, int clientType, String version) {
        List<Banner> banners = eventServiceApi.listBanners(cityId, Configuration.getInt("PageSize.Banner"));
        List<Banner> filteredBanners = new ArrayList<Banner>();
        for (Banner banner : banners) {
            if (banner.getPlatform() != 0 && banner.getPlatform() != clientType) continue;
            if (banner.getPlatform() == ClientType.APP &&
                    !StringUtils.isBlank(banner.getVersion()) &&
                    !StringUtils.isBlank(version) &&
                    banner.getVersion().compareTo(version) > 0) continue;
            banner.setCover(completeImg(banner.getCover()));
            banner.setAction(buildAction(banner.getAction(), clientType));
            filteredBanners.add(banner);
        }

        return filteredBanners;
    }

    protected List<Icon> getIcons(int cityId, int clientType, String version) {
        List<Icon> icons = eventServiceApi.listIcons(cityId, Configuration.getInt("PageSize.Icon"));
        List<Icon> filteredIcons = new ArrayList<Icon>();
        for (Icon icon : icons) {
            if (icon.getPlatform() != 0 && icon.getPlatform() != clientType) continue;
            if (icon.getPlatform() == ClientType.APP &&
                    !StringUtils.isBlank(icon.getVersion()) &&
                    !StringUtils.isBlank(version) &&
                    icon.getVersion().compareTo(version) > 0) continue;
            icon.setImg(completeImg(icon.getImg()));
            icon.setAction(buildAction(icon.getAction(), clientType));
            filteredIcons.add(icon);
        }

        return filteredIcons;
    }

    protected List<Event> getEvents(int cityId, int clientType, String version) {
        List<Event> events = eventServiceApi.listEvents(cityId, Configuration.getInt("PageSize.Event"));
        List<Event> filteredEvents = new ArrayList<Event>();
        for (Event event : events) {
            if (event.getPlatform() != 0 && event.getPlatform() != clientType) continue;
            if (event.getPlatform() == ClientType.APP &&
                    !StringUtils.isBlank(event.getVersion()) &&
                    !StringUtils.isBlank(version) &&
                    event.getVersion().compareTo(version) > 0) continue;
            event.setImg(completeImg(event.getImg()));
            event.setAction(buildAction(event.getAction(), clientType));
            filteredEvents.add(event);
        }

        if (filteredEvents.size() % 2 != 0) {
            return filteredEvents.subList(0, filteredEvents.size() - 1);
        }

        return filteredEvents;
    }
}
