package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.http.MomiaHttpRequest;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.mapi.api.v1.misc.Xml;
import cn.momia.mapi.common.util.XmlUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/callback")
public class CallbackV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackV1Api.class);

    @RequestMapping(value = "/alipay", method = RequestMethod.POST, produces = "text/plain")
    public String alipayCallback(HttpServletRequest request) {
        try {
            Map<String, String> params = extractParams(request.getParameterMap());
            ResponseMessage response = executeRequest(MomiaHttpRequest.POST(url("callback/alipay"), params));
            if (response.successful()) return "success";
        } catch (Exception e) {
            LOGGER.error("ali pay callback error", e);
        }

        return "fail";
    }

    private Map<String, String> extractParams(Map<String, String[]> httpParams) {
        Map<String, String> params = new HashMap<String, String>();
        for (Map.Entry<String, String[]> entry : httpParams.entrySet()) {
            String[] values = entry.getValue();
            if (values.length <= 0) continue;
            params.put(entry.getKey(), entry.getValue()[0]);
        }

        return params;
    }

    @RequestMapping(value = "/wechatpay", method = RequestMethod.POST, produces = "application/xml")
    public Xml wechatpayCallback(HttpServletRequest request) {
        try {
            Map<String, String> params = XmlUtil.xmlToParams(IOUtils.toString(request.getInputStream()));
            ResponseMessage response = executeRequest(MomiaHttpRequest.POST(url("callback/wechatpay"), params));
            if (response.successful()) return new Xml("SUCCESS", "OK");
        } catch (Exception e) {
            LOGGER.error("wechat pay callback error", e);
        }

        return new Xml("FAIL", "ERROR");
    }
}
