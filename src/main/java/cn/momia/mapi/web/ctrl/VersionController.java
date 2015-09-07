package cn.momia.mapi.web.ctrl;

import cn.momia.common.webapp.ctrl.BaseController;

import javax.servlet.http.HttpServletRequest;

public abstract class VersionController extends BaseController {
    public String forward(HttpServletRequest request) {
        return forward(request, request.getRequestURI());
    }

    public String forward(HttpServletRequest request, String uri) {
        return "forward:/" + getApiVersion(request) + uri;
    }

    private String getApiVersion(HttpServletRequest request) {
        return "v1";
    }
}
