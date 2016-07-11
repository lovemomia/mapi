package cn.momia.mapi.api.index;

import beans.ConfigService;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class AbstractIndexApi extends AbstractApi {
    @Autowired private ConfigService configService;

    protected List<Config.Banner> getBanners(int cityId, int platform, String clientVersion) {
        List<Config.Banner> banners = configService.listBanners(cityId);
        List<Config.Banner> filteredBanners = new ArrayList<Config.Banner>();
        for (Config.Banner banner : banners) {
            if (banner.isInvalid(platform, clientVersion)) continue;
            banner.setCover(completeImg(banner.getCover()));
            banner.setAction(buildAction(banner.getAction(), platform));
            filteredBanners.add(banner);
        }

        int maxCount = Configuration.getInt("PageSize.Banner");
        return filteredBanners.size() > maxCount ? filteredBanners.subList(0, maxCount) : filteredBanners;
    }

    protected List<Config.Icon> getIcons(int cityId, int platform, String clientVersion) {
        List<Config.Icon> icons = configService.listIcons(cityId);
        List<Config.Icon> filteredIcons = new ArrayList<Config.Icon>();
        for (Config.Icon icon : icons) {
            if (icon.isInvalid(platform, clientVersion)) continue;
            icon.setImg(completeImg(icon.getImg()));
            icon.setAction(buildAction(icon.getAction(), platform));
            filteredIcons.add(icon);
        }

        int maxCount= Configuration.getInt("PageSize.Icon");
        return filteredIcons.size() > maxCount ? filteredIcons.subList(0, maxCount) : filteredIcons;
    }

    protected List<Config.Event> getEvents(int cityId, int platform, String clientVersion, int type) {
        List<Config.Event> events = configService.listEvents(cityId);
        List<Config.Event> filteredEvents = new ArrayList<Config.Event>();
        for (Config.Event event : events) {
            if (event.isInvalid(platform, clientVersion) || (type > 0 && event.getType() != type)) continue;
            event.setImg(completeImg(event.getImg()));
            event.setAction(buildAction(event.getAction(), platform));
            filteredEvents.add(event);
        }

        int maxCount = Configuration.getInt("PageSize.Event");
        if (filteredEvents.size() > maxCount) filteredEvents = filteredEvents.subList(0, maxCount);

        filteredEvents = filteredEvents.size() % 2 != 0 ? filteredEvents.subList(0, filteredEvents.size() - 1) : filteredEvents;
        if (filteredEvents.size() > 2) filteredEvents = filteredEvents.subList(0, 2);

        return filteredEvents;
    }

    protected List<Config.Recommend> getRecommends(int cityId, int platform, String clientVersion) {
        List<Config.Recommend> recommends = configService.listRecommends(cityId);
        List<Config.Recommend> filteredRecommends = new ArrayList<Config.Recommend>();
        for (Config.Recommend recommend : recommends) {
            if (recommend.isInvalid(platform, clientVersion)) continue;
            recommend.setCover(completeLargeImg(recommend.getCover()));
            recommend.setAction(buildAction(recommend.getAction(), platform));
            filteredRecommends.add(recommend);
        }

        int maxCount= Configuration.getInt("PageSize.Recommend");
        return filteredRecommends.size() > maxCount ? filteredRecommends.subList(0, maxCount) : filteredRecommends;
    }
}
