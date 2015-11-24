package cn.momia.mapi.web.ctrl;

import cn.momia.common.webapp.ctrl.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@Controller
public class MApiController extends BaseController {
    private static final Pattern PATTERN = Pattern.compile("^/v\\d+/.+");

    @RequestMapping(value = "/m/**", method = { RequestMethod.GET, RequestMethod.POST })
    public String processMRequest(HttpServletRequest request) {
        return forward(request, request.getRequestURI().substring(2));
    }

    public String forward(HttpServletRequest request, String uri) {
        if (PATTERN.matcher(uri).find()) return "forward:" + uri;
        return "forward:/v1" + uri;
    }

    @RequestMapping(value = "/**", method = { RequestMethod.GET, RequestMethod.POST })
    public String processRequest(HttpServletRequest request) {
        return forward(request, request.getRequestURI());
    }

    @RequestMapping(value = "/v{\\d+}/**", method = { RequestMethod.GET, RequestMethod.POST })
    public String notFound() {
        return "forward:/error/404";
    }
}
