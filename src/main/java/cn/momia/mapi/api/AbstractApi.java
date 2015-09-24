package cn.momia.mapi.api;

import cn.momia.common.client.ClientType;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.common.webapp.ctrl.BaseController;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

public abstract class AbstractApi extends BaseController {
    protected int getClientType(HttpServletRequest request) {
        return StringUtils.isBlank(request.getParameter("terminal")) ? ClientType.WAP : ClientType.APP;
    }

    protected String buildLink(String uri, int clientType) {
        if (ClientType.isApp(clientType)) return Configuration.getString("AppConf.Name") + "://web?url=" + URLEncoder.encode(buildFullUrl(uri));
        return buildFullUrl(uri);
    }

    private String buildFullUrl(String uri) {
        if (uri.startsWith("http")) return uri;

        if (!uri.startsWith("/")) uri = "/" + uri;
        return Configuration.getString("AppConf.WapDomain") + "/m" + uri;
    }
}
