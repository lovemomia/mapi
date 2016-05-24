package cn.momia.mapi.web.ctrl;

import cn.momia.common.webapp.ctrl.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

@Controller
public class MApiController extends BaseController {
    private static final Pattern PATTERN = Pattern.compile("^/v\\d+/.+");

    @RequestMapping(value = "/m/**", method = { RequestMethod.GET, RequestMethod.POST })
    public String processMRequest(HttpServletRequest request) {
        return forward(request, request.getRequestURI().substring(2));
    }

    private String forward(HttpServletRequest request, String uri) {
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

    public static void main(String[]  args) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("/data/vipcard.csv"))));
        for (int i = 1500; i < 2000; i++) {
            pw.println("00131" + (i+1) + "," + password());
        }
        pw.close();
    }

    private static String password() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}
