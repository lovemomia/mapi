package cn.momia.mapi.api;

import cn.momia.common.client.ClientType;
import cn.momia.common.webapp.ctrl.BaseController;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractApi extends BaseController {
    protected static final int IMAGE_LARGE = 1;
    protected static final int IMAGE_MIDDLE = 2;
    protected static final int IMAGE_SMALL = 3;

    protected int getClientType(HttpServletRequest request) {
        return StringUtils.isBlank(request.getParameter("terminal")) ? ClientType.WAP : ClientType.APP;
    }
}
