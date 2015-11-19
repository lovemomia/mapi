package cn.momia.mapi.api;

import cn.momia.common.client.ClientType;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.common.webapp.ctrl.BaseController;
import cn.momia.image.api.ImageFile;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractApi extends BaseController {
    protected int getClientType(HttpServletRequest request) {
        return StringUtils.isBlank(request.getParameter("terminal")) ? ClientType.WAP : ClientType.APP;
    }

    protected String buildAction(String uri, int clientType) {
        if (ClientType.isApp(clientType)) {
            if (uri.startsWith("http")) return Configuration.getString("AppConf.Name") + "://web?url=" + URLEncoder.encode(uri);
            return Configuration.getString("AppConf.Name") + "://" + uri;
        }

        return buildFullUrl(uri);
    }

    private String buildFullUrl(String uri) {
        if (uri.startsWith("http")) return uri;

        if (!uri.startsWith("/")) uri = "/" + uri;
        return uri;
    }

    protected List<String> completeImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(ImageFile.url(img));
        }

        return completedImgs;
    }

    protected List<String> completeLargeImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(ImageFile.largeUrl(img));
        }

        return completedImgs;
    }

    protected List<String> completeMiddleImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(ImageFile.middleUrl(img));
        }

        return completedImgs;
    }

    protected List<String> completeSmallImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(ImageFile.smallUrl(img));
        }

        return completedImgs;
    }
}
