package cn.momia.mapi.api.index;

import cn.momia.api.operate.ConfigServiceApi;
import cn.momia.api.operate.dto.Banner;
import cn.momia.api.operate.dto.Event;
import cn.momia.api.operate.dto.Icon;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class AbstractIndexApi extends AbstractApi {
    @Autowired private ConfigServiceApi configServiceApi;

    protected List<Banner> getBanners(int cityId, int platform, String clientVersion) {
        List<Banner> banners = configServiceApi.listBanners(cityId);
        List<Banner> filteredBanners = new ArrayList<Banner>();
        for (Banner banner : banners) {
            if (banner.isInvalid(platform, clientVersion)) continue;
            banner.setCover(completeImg(banner.getCover()));
            banner.setAction(buildAction(banner.getAction(), platform));
            filteredBanners.add(banner);
        }

        int maxCount = Configuration.getInt("PageSize.Banner");
        return filteredBanners.size() > maxCount ? filteredBanners.subList(0, maxCount) : filteredBanners;
    }

    protected List<Icon> getIcons(int cityId, int platform, String clientVersion) {
        List<Icon> icons = configServiceApi.listIcons(cityId);
        List<Icon> filteredIcons = new ArrayList<Icon>();
        for (Icon icon : icons) {
            if (icon.isInvalid(platform, clientVersion)) continue;
            icon.setImg(completeImg(icon.getImg()));
            icon.setAction(buildAction(icon.getAction(), platform));
            filteredIcons.add(icon);
        }

        int maxCount= Configuration.getInt("PageSize.Icon");
        return filteredIcons.size() > maxCount ? filteredIcons.subList(0, maxCount) : filteredIcons;
    }

    protected List<Event> getEvents(int cityId, int platform, String clientVersion) {
        List<Event> events = configServiceApi.listEvents(cityId);
        List<Event> filteredEvents = new ArrayList<Event>();
        for (Event event : events) {
            if (event.isInvalid(platform, clientVersion)) continue;
            event.setImg(completeImg(event.getImg()));
            event.setAction(buildAction(event.getAction(), platform));
            filteredEvents.add(event);
        }

        int maxCount = Configuration.getInt("PageSize.Event");
        if (filteredEvents.size() > maxCount) filteredEvents = filteredEvents.subList(0, maxCount);

        return filteredEvents.size() % 2 != 0 ? filteredEvents.subList(0, filteredEvents.size() - 1) : filteredEvents;
    }
}