package cn.momia.mapi.api.course;

import cn.momia.api.course.CouponServiceApi;
import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.OrderServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.subject.Subject;
import cn.momia.api.course.dto.subject.SubjectSku;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.course.dto.subject.SubjectOrder;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Contact;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/subject")
public class SubjectV1Api extends AbstractApi {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private CouponServiceApi couponServiceApi;
    @Autowired private OrderServiceApi orderServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Subject subject = subjectServiceApi.get(id);
        completeLargeImg(subject);

        PagedList<Course> courses = courseServiceApi.query(id, 0, 2);
        completeMiddleCoursesImgs(courses.getList());

        PagedList<UserCourseComment> comments = subjectServiceApi.queryCommentsBySubject(id, 0, 2);
        completeCourseCommentsImgs(comments.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("courses", courses);
        if (!comments.getList().isEmpty()) responseJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public MomiaHttpResponse list(@RequestParam(value = "city") int cityId) {
        if (cityId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<Subject> subjects = subjectServiceApi.list(cityId);
        for (Subject subject : subjects) {
            completeLargeImg(subject);
        }

        return MomiaHttpResponse.SUCCESS(subjects);
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public MomiaHttpResponse listCourses(@RequestParam long id,
                                         @RequestParam(value = "pid", required = false, defaultValue = "0") long packageId,
                                         @RequestParam(required = false, defaultValue = "0") int age,
                                         @RequestParam(required = false, defaultValue = "0") int sort,
                                         @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        // FIXME Magic Number
        PagedList<Course> courses = courseServiceApi.query(id, packageId, 1, 100, 0, start, Configuration.getInt("PageSize.Course"));
        completeMiddleCoursesImgs(courses.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("ages", buildAgeRanges());
        responseJson.put("sorts", buildSortTypes());
        responseJson.put("currentAge", 0);
        responseJson.put("currentSort", 0);
        responseJson.put("courses", courses);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    private JSONArray buildAgeRanges() {
        JSONArray ageRanges = new JSONArray();
        JSONObject ageRange = new JSONObject();
        ageRange.put("id", 0);
        ageRange.put("min", 1);
        ageRange.put("max", 100);
        ageRange.put("text", "全部");
        ageRanges.add(ageRange);

        return ageRanges;
    }

    private JSONArray buildSortTypes() {
        JSONArray sortTypes = new JSONArray();
        JSONObject sortType = new JSONObject();
        sortType.put("id", 0);
        sortType.put("text", "默认");
        sortTypes.add(sortType);

        return sortTypes;
    }

    @RequestMapping(value = "/comment/list", method = RequestMethod.GET)
    public MomiaHttpResponse listComments(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<UserCourseComment> pageComments = subjectServiceApi.queryCommentsBySubject(id, start, Configuration.getInt("PageSize.CourseComment"));
        completeCourseCommentsImgs(pageComments.getList());

        return MomiaHttpResponse.SUCCESS(pageComments);
    }

    @RequestMapping(value = "/sku", method = RequestMethod.GET)
    public MomiaHttpResponse sku(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Subject subject = subjectServiceApi.get(id);

        List<SubjectSku> skus = subjectServiceApi.querySkus(id);
        Contact contact = userServiceApi.getContact(utoken);

        List<SubjectSku> subjectSkus = new ArrayList<SubjectSku>();
        for (SubjectSku sku : skus) {
            if (subject.getType() == Subject.Type.NORMAL) {
                if (sku.getCourseId() <= 0) subjectSkus.add(sku);
            } else if (subject.getType() == Subject.Type.TRIAL) {
                if (sku.getCourseId() > 0) subjectSkus.add(sku);
            }
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("skus", subjectSkus);
        responseJson.put("contact", contact);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public MomiaHttpResponse order(@RequestParam String utoken, @RequestParam String order) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(order)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject orderJson = JSON.parseObject(order);

        User user = userServiceApi.get(utoken);
        orderJson.put("userId", user.getId());

        JSONObject contactJson = orderJson.getJSONObject("contact");
        orderJson.put("contact", contactJson.getString("name"));
        orderJson.put("mobile", contactJson.getString("mobile"));

        JSONArray packagesJson = new JSONArray();
        JSONArray skusJson = orderJson.getJSONArray("skus");
        for (int i = 0; i < skusJson.size(); i++) {
            JSONObject skuJson = skusJson.getJSONObject(i);
            orderJson.put("subjectId", skuJson.getLong("subjectId"));
            int count = skuJson.getInteger("count");
            for (int j = 0; j < count; j++) {
                JSONObject packageJson = new JSONObject();
                packageJson.put("skuId", skuJson.getLong("id"));
                packagesJson.add(packageJson);
            }
        }
        orderJson.put("packages", packagesJson);

        return MomiaHttpResponse.SUCCESS(orderServiceApi.placeOrder(orderJson));
    }

    @RequestMapping(value = "/order/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteOrder(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        if (!orderServiceApi.deleteOrder(utoken, orderId)) return MomiaHttpResponse.FAILED("删除订单失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/order/refund", method = RequestMethod.POST)
    public MomiaHttpResponse refund(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        if (!orderServiceApi.refundOrder(utoken, orderId)) return MomiaHttpResponse.FAILED("申请退款失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/order/coupon", method = RequestMethod.GET)
    public MomiaHttpResponse coupon(@RequestParam String utoken,
                                    @RequestParam(value = "oid") long orderId,
                                    @RequestParam(value = "coupon") long userCouponId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || userCouponId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(couponServiceApi.coupon(utoken, orderId, userCouponId));
    }

    @RequestMapping(value = "/order/detail", method = RequestMethod.GET)
    public MomiaHttpResponse orderDetail(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        SubjectOrder order = orderServiceApi.get(utoken, orderId);
        order.setCover(completeMiddleImg(order.getCover()));

        return MomiaHttpResponse.SUCCESS(order);
    }

    @RequestMapping(value = "/order/gift/send", method = RequestMethod.POST)
    public MomiaHttpResponse sendGift(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(orderServiceApi.sendGift(utoken, orderId));
    }

    @RequestMapping(value = "/order/gift/receive", method = RequestMethod.POST)
    public MomiaHttpResponse receiveGift(@RequestParam String utoken,
                                         @RequestParam(value = "oid") long orderId,
                                         @RequestParam long expired,
                                         @RequestParam String giftsign) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || expired <= 0 || StringUtils.isBlank(giftsign)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(orderServiceApi.receiveGift(utoken, orderId, expired, giftsign));
    }

    @RequestMapping(value = "/favor", method = RequestMethod.POST)
    public MomiaHttpResponse favor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        if (!subjectServiceApi.favor(user.getId(), id)) return MomiaHttpResponse.FAILED("添加收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unfavor", method = RequestMethod.POST)
    public MomiaHttpResponse unfavor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        if (!subjectServiceApi.unfavor(user.getId(), id)) return MomiaHttpResponse.FAILED("取消收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }
}
