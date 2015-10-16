package cn.momia.mapi.api.v1.course;

import cn.momia.api.base.MetaUtil;
import cn.momia.api.base.dto.AgeRangeDto;
import cn.momia.api.base.dto.SortTypeDto;
import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.api.course.dto.SubjectSkuDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.ContactDto;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.util.XmlUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/subject")
public class SubjectV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectV1Api.class);

    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam long id) {
        SubjectDto subject = processSubject(subjectServiceApi.get(id));
        PagedList<CourseDto> courses = processPagedCourses(courseServiceApi.query(id, 0, 2));

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("courses", courses);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public MomiaHttpResponse listCourses(@RequestParam long id,
                                         @RequestParam(required = false, defaultValue = "0") int age,
                                         @RequestParam(required = false, defaultValue = "0") int sort,
                                         @RequestParam int start) {
        AgeRangeDto ageRange = MetaUtil.getAgeRange(age);
        SortTypeDto sortType = MetaUtil.getSortType(sort);

        PagedList<CourseDto> courses = courseServiceApi.query(id, ageRange.getMin(), ageRange.getMax(), sortType.getId(), start, Configuration.getInt("PageSize.Course"));

        JSONObject responseJson = new JSONObject();
        responseJson.put("ages", MetaUtil.listAgeRanges());
        responseJson.put("sorts", MetaUtil.listSortTypes());
        responseJson.put("currentAge", ageRange.getId());
        responseJson.put("currentSort", sortType.getId());
        responseJson.put("courses", processPagedCourses(courses));

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/sku", method = RequestMethod.GET)
    public MomiaHttpResponse order(@RequestParam String utoken, @RequestParam long id) {
        List<SubjectSkuDto> skus = subjectServiceApi.listSkus(id);
        ContactDto contact = userServiceApi.getContact(utoken);

        JSONObject responseJson = new JSONObject();
        responseJson.put("skus", skus);
        responseJson.put("contact", contact);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public MomiaHttpResponse order(@RequestParam String utoken, @RequestParam String order) {
        JSONObject orderJson = JSON.parseObject(order);

        UserDto user = userServiceApi.get(utoken);
        orderJson.put("userId", user.getId());

        Map<Long, Integer> counts = new HashMap<Long, Integer>();
        JSONArray skusJson = orderJson.getJSONArray("skus");
        for (int i = 0; i < skusJson.size(); i++) {
            JSONObject skuJson = skusJson.getJSONObject(i);
            orderJson.put("subjectId", skuJson.getLong("subjectId"));
            counts.put(skuJson.getLong("id"), skuJson.getInteger("count"));
        }
        orderJson.put("counts", counts);

        return MomiaHttpResponse.SUCCESS(subjectServiceApi.placeOrder(orderJson));
    }

    @RequestMapping(value = "/payment/prepay/alipay", method = RequestMethod.POST)
    public MomiaHttpResponse prepayAlipay(@RequestParam String utoken,
                                          @RequestParam(value = "oid") long orderId,
                                          @RequestParam(defaultValue = "app") String type) {
        return MomiaHttpResponse.SUCCESS(subjectServiceApi.prepayAlipay(utoken, orderId, type));
    }

    @RequestMapping(value = "/payment/prepay/weixin", method = RequestMethod.POST)
    public MomiaHttpResponse prepayWeixin(@RequestParam String utoken,
                                          @RequestParam(value = "oid") long orderId,
                                          @RequestParam(defaultValue = "app") final String type,
                                          @RequestParam(required = false) String code) {
        return MomiaHttpResponse.SUCCESS(subjectServiceApi.prepayWeixin(utoken, orderId, type, code));
    }

    @RequestMapping(value = "/payment/callback/alipay", method = RequestMethod.POST, produces = "text/plain")
    public String callbackAlipay(HttpServletRequest request) {
        try {
            Map<String, String> params = extractParams(request);
            if (subjectServiceApi.callbackAlipay(params)) return "success";
        } catch (Exception e) {
            LOGGER.error("ali pay callback error", e);
        }

        LOGGER.error("ali pay callback failure");

        return "fail";
    }

    @RequestMapping(value = "/payment/callback/weixin", method = RequestMethod.POST, produces = "application/xml")
    public String callbackWeixin(HttpServletRequest request) {
        try {
            Map<String, String> params = XmlUtil.xmlToMap(IOUtils.toString(request.getInputStream()));
            if (subjectServiceApi.callbackWeixin(params)) return WechatpayResponse.SUCCESS;
        } catch (Exception e) {
            LOGGER.error("wechat pay callback error", e);
        }

        LOGGER.error("wechat pay callback failure");

        return WechatpayResponse.FAILED;
    }

    private static class WechatpayResponse {
        public static String SUCCESS = new WechatpayResponse("SUCCESS", "OK").toString();
        public static String FAILED = new WechatpayResponse("FAIL", "ERROR").toString();

        private String return_code;
        private String return_msg;

        public WechatpayResponse(String return_code, String return_msg) {
            this.return_code = return_code;
            this.return_msg = return_msg;
        }

        @Override
        public String toString() {
            return "<xml><return_code>" + return_code + "</return_code><return_msg>" + return_msg + "</return_msg></xml>";
        }
    }

    @RequestMapping(value = "/payment/check", method = RequestMethod.POST)
    public MomiaHttpResponse check(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        return MomiaHttpResponse.SUCCESS(subjectServiceApi.checkPayment(utoken, orderId));
    }
}
