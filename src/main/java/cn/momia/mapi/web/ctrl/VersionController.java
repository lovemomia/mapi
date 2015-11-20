package cn.momia.mapi.web.ctrl;

import cn.momia.common.webapp.ctrl.BaseController;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public abstract class VersionController extends BaseController {
    private static final Pattern  PATTERN = Pattern.compile("^/v\\d+/.+");

    public String forward(HttpServletRequest request) {
        return forward(request, request.getRequestURI());
    }

    public String forward(HttpServletRequest request, String uri) {
        if (PATTERN.matcher(uri).find()) return "forward:" + uri;
        return "forward:/v1" + uri;
    }
}
