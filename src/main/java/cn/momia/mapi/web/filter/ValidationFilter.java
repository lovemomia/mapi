package cn.momia.mapi.web.filter;

import cn.momia.common.webapp.config.Configuration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ValidationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (isUserAgentMissing(httpRequest)) {
            forwardErrorPage(request, response, 400);
            return;
        }

        if (needParamsValidation(httpRequest)) {
            if (isParamMissing(httpRequest)) {
                forwardErrorPage(request, response, 400);
                return;
            }

            if (isInvalidSign(httpRequest)) {
                forwardErrorPage(request, response, 403);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isUserAgentMissing(HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("user-agent");
        return StringUtils.isBlank(userAgent);
    }

    private boolean needParamsValidation(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !(uri.startsWith("/payment/callback/") || uri.startsWith("/m/"));
    }

    private boolean isParamMissing(HttpServletRequest httpRequest) {
        String version = httpRequest.getParameter("v");
        String teminal = httpRequest.getParameter("terminal");
        String os = httpRequest.getParameter("os");
        String device = httpRequest.getParameter("device");
        String channel = httpRequest.getParameter("channel");
        String net = httpRequest.getParameter("net");
        String sign = httpRequest.getParameter("sign");

        return (StringUtils.isBlank(version) ||
                StringUtils.isBlank(teminal) ||
                StringUtils.isBlank(os) ||
                StringUtils.isBlank(device) ||
                StringUtils.isBlank(channel) ||
                StringUtils.isBlank(net) ||
                StringUtils.isBlank(sign));
    }

    private boolean isInvalidSign(HttpServletRequest httpRequest) {
        List<String> kvs = new ArrayList<String>();
        Map<String, String[]> httpParams = httpRequest.getParameterMap();
        for (Map.Entry<String, String[]> entry : httpParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            if (key.equalsIgnoreCase("sign") || StringUtils.isBlank(value)) continue;

            kvs.add(key + "=" + value);
        }
        Collections.sort(kvs);
        kvs.add("key=" + Configuration.getString("Validation.Key"));

        String sign = httpRequest.getParameter("sign");

        return !sign.equals(DigestUtils.md5Hex(StringUtils.join(kvs, "")));
    }

    private void forwardErrorPage(ServletRequest request, ServletResponse response, int errorCode) throws ServletException, IOException {
        request.getRequestDispatcher("/error/" + errorCode).forward(request, response);
    }

    @Override
    public void destroy() {}
}
