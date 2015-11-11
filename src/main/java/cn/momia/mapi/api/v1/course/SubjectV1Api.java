package cn.momia.mapi.api.v1.course;

import cn.momia.api.base.MetaUtil;
import cn.momia.api.base.dto.AgeRangeDto;
import cn.momia.api.base.dto.SortTypeDto;
import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.CourseCommentDto;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.api.course.dto.SubjectSkuDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.ContactDto;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/subject")
public class SubjectV1Api extends AbstractV1Api {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        SubjectDto subject = subjectServiceApi.get(id);
        subject.setCover(ImageFile.largeUrl(subject.getCover()));
        subject.setImgs(completeLargeImgs(subject.getImgs()));

        PagedList<CourseDto> courses = courseServiceApi.query(id, 0, 2);
        processCourses(courses.getList());

        PagedList<CourseCommentDto> comments = courseServiceApi.queryCommentsBySubject(id, 0, 2);
        processCourseComments(comments.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("courses", courses);
        if (!comments.getList().isEmpty()) responseJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    private void processCourses(List<CourseDto> courses) {
        for (CourseDto course : courses) {
            course.setCover(ImageFile.middleUrl(course.getCover()));
        }
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public MomiaHttpResponse listCourses(@RequestParam long id,
                                         @RequestParam(value = "pid", required = false, defaultValue = "0") long packageId,
                                         @RequestParam(required = false, defaultValue = "0") int age,
                                         @RequestParam(required = false, defaultValue = "0") int sort,
                                         @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        AgeRangeDto ageRange = MetaUtil.getAgeRange(age);
        SortTypeDto sortType = MetaUtil.getSortType(sort);

        PagedList<CourseDto> courses = courseServiceApi.query(id, packageId, ageRange.getMin(), ageRange.getMax(), sortType.getId(), start, Configuration.getInt("PageSize.Course"));
        processCourses(courses.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("ages", MetaUtil.listAgeRanges());
        responseJson.put("sorts", MetaUtil.listSortTypes());
        responseJson.put("currentAge", ageRange.getId());
        responseJson.put("currentSort", sortType.getId());
        responseJson.put("courses", courses);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/comment/list", method = RequestMethod.GET)
    public MomiaHttpResponse listComments(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<CourseCommentDto> pageComments = courseServiceApi.queryCommentsBySubject(id, start, Configuration.getInt("PageSize.CourseComment"));
        processCourseComments(pageComments.getList());

        return MomiaHttpResponse.SUCCESS(pageComments);
    }

    @RequestMapping(value = "/sku", method = RequestMethod.GET)
    public MomiaHttpResponse sku(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<SubjectSkuDto> skus = subjectServiceApi.querySkus(id);
        ContactDto contact = userServiceApi.getContact(utoken);

        JSONObject responseJson = new JSONObject();
        responseJson.put("skus", skus);
        responseJson.put("contact", contact);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public MomiaHttpResponse order(@RequestParam String utoken, @RequestParam String order) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(order)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject orderJson = JSON.parseObject(order);

        UserDto user = userServiceApi.get(utoken);
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

        return MomiaHttpResponse.SUCCESS(subjectServiceApi.placeOrder(orderJson));
    }

    @RequestMapping(value = "/order/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteOrder(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        if (!subjectServiceApi.deleteOrder(utoken, orderId)) return MomiaHttpResponse.FAILED("删除订单失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/order/refund", method = RequestMethod.POST)
    public MomiaHttpResponse refund(@RequestParam String utoken, @RequestParam(value = "oid") long orderId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        if (!subjectServiceApi.refundOrder(utoken, orderId)) return MomiaHttpResponse.FAILED("申请退款失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/order/coupon", method = RequestMethod.GET)
    public MomiaHttpResponse coupon(@RequestParam String utoken,
                                    @RequestParam(value = "oid") long orderId,
                                    @RequestParam(value = "coupon") long userCouponId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (orderId <= 0 || userCouponId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(subjectServiceApi.coupon(utoken, orderId, userCouponId));
    }

    @RequestMapping(value = "/favor", method = RequestMethod.POST)
    public MomiaHttpResponse favor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        if (!subjectServiceApi.favor(user.getId(), id)) return MomiaHttpResponse.FAILED("添加收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unfavor", method = RequestMethod.POST)
    public MomiaHttpResponse unfavor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        if (!subjectServiceApi.unfavor(user.getId(), id)) return MomiaHttpResponse.FAILED("取消收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }
}
