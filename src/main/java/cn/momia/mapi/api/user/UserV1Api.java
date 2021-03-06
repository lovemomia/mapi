package cn.momia.mapi.api.user;

import cn.momia.api.course.CouponServiceApi;
import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.OrderServiceApi;
import cn.momia.api.course.dto.course.BookedCourse;
import cn.momia.api.course.dto.subject.SubjectPackage;
import cn.momia.api.course.dto.subject.SubjectOrder;
import cn.momia.api.course.dto.comment.TimelineUnit;
import cn.momia.api.course.dto.coupon.UserCoupon;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/user")
public class UserV1Api extends AbstractApi {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private CouponServiceApi couponServiceApi;
    @Autowired private OrderServiceApi orderServiceApi;
    @Autowired private ImServiceApi imServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse getUser(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.get(utoken)));
    }

    @RequestMapping(value = "/nickname", method = RequestMethod.POST)
    public MomiaHttpResponse updateNickName(@RequestParam String utoken, @RequestParam(value = "nickname") String nickName) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(nickName)) return MomiaHttpResponse.FAILED("用户昵称不能为空");
        if (nickName.contains("官方")) return MomiaHttpResponse.FAILED("用户昵称不能包含“官方”");

        User user = completeUserImgs(userServiceApi.updateNickName(utoken, nickName));
        imServiceApi.updateImNickName(user.getId(), user.getNickName());

        return MomiaHttpResponse.SUCCESS(user);
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public MomiaHttpResponse updateAvatar(@RequestParam String utoken, @RequestParam String avatar) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(avatar)) return MomiaHttpResponse.FAILED("用户头像不能为空");

        User user = completeUserImgs(userServiceApi.updateAvatar(utoken, avatar));
        imServiceApi.updateImAvatar(user.getId(), user.getAvatar());

        return MomiaHttpResponse.SUCCESS(user);
    }

    @RequestMapping(value = "/cover", method = RequestMethod.POST)
    public MomiaHttpResponse updateCover(@RequestParam String utoken, @RequestParam String cover) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(cover)) return MomiaHttpResponse.FAILED("用户封面图不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.updateCover(utoken, cover)));
    }

    @RequestMapping(value = "/name", method = RequestMethod.POST)
    public MomiaHttpResponse updateName(@RequestParam String utoken, @RequestParam String name) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(name)) return MomiaHttpResponse.FAILED("用户名字不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.updateName(utoken, name)));
    }

    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    public MomiaHttpResponse updateSex(@RequestParam String utoken, @RequestParam String sex) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(sex) || MomiaUtil.isInvalidSex(sex)) return MomiaHttpResponse.FAILED("无效的用户性别");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.updateSex(utoken, sex)));
    }

    @RequestMapping(value = "/birthday", method = RequestMethod.POST)
    public MomiaHttpResponse updateBirthday(@RequestParam String utoken, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthday) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (birthday == null) return MomiaHttpResponse.FAILED("出生日期不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.updateBirthday(utoken, birthday)));
    }

    @RequestMapping(value = "/city", method = RequestMethod.POST)
    public MomiaHttpResponse updateCity(@RequestParam String utoken, @RequestParam(value = "city") int cityId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (cityId <= 0) return MomiaHttpResponse.FAILED("无效的城市ID");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.updateCity(utoken, cityId)));
    }

    @RequestMapping(value = "/region", method = RequestMethod.POST)
    public MomiaHttpResponse updateRegion(@RequestParam String utoken, @RequestParam(value = "region") int regionId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (regionId <= 0) return MomiaHttpResponse.FAILED("无效的区域ID");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.updateRegion(utoken, regionId)));
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    public MomiaHttpResponse updateAddress(@RequestParam String utoken, @RequestParam String address) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(address)) return MomiaHttpResponse.FAILED("地址不能为空");

        return MomiaHttpResponse.SUCCESS(completeUserImgs(userServiceApi.updateAddress(utoken, address)));
    }

    @RequestMapping(value = "/course/notfinished", method = RequestMethod.GET)
    public MomiaHttpResponse listNotFinished(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        User user = userServiceApi.get(utoken);
        PagedList<BookedCourse> courses = courseServiceApi.queryNotFinishedByUser(user.getId(), start, Configuration.getInt("PageSize.Course"));
        completeMiddleCoursesImgs(courses.getList());

        return MomiaHttpResponse.SUCCESS(courses);
    }

    @RequestMapping(value = "/course/finished", method = RequestMethod.GET)
    public MomiaHttpResponse listFinished(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        User user = userServiceApi.get(utoken);
        PagedList<BookedCourse> courses = courseServiceApi.queryFinishedByUser(user.getId(), start, Configuration.getInt("PageSize.Course"));
        completeMiddleCoursesImgs(courses.getList());

        return MomiaHttpResponse.SUCCESS(courses);
    }


    @RequestMapping(value = "/booked/sku", method = RequestMethod.GET)
    public MomiaHttpResponse getSku(@RequestParam String utoken, @RequestParam(value = "bid") long bookingId) {
        if (bookingId <= 0) return MomiaHttpResponse.FAILED("无效的BookingID");

        User user = userServiceApi.get(utoken);
        return MomiaHttpResponse.SUCCESS(courseServiceApi.getBookedSku(user.getId(), bookingId));
    }

    @RequestMapping(value = "/bookable", method = RequestMethod.GET)
    public MomiaHttpResponse listBookableOrders(@RequestParam String utoken,
                                                @RequestParam(value = "oid", required = false, defaultValue = "0") long orderId,
                                                @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        PagedList<SubjectPackage> packages = orderServiceApi.listBookable(utoken, orderId, start, Configuration.getInt("PageSize.Subject"));
        for (SubjectPackage orderPackage : packages.getList()) {
            orderPackage.setCover(completeMiddleImg(orderPackage.getCover()));
        }

        return MomiaHttpResponse.SUCCESS(packages);
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public MomiaHttpResponse listOrders(@RequestParam String utoken, @RequestParam int status, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (status <= 0) return MomiaHttpResponse.FAILED("无效的订单状态值");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        PagedList<SubjectOrder> orders = orderServiceApi.listOrders(utoken, status, start, Configuration.getInt("PageSize.Order"));
        for (SubjectOrder order : orders.getList()) {
            order.setCover(completeMiddleImg(order.getCover()));
        }

        return MomiaHttpResponse.SUCCESS(orders);
    }

    @RequestMapping(value = "/coupon", method = RequestMethod.GET)
    public MomiaHttpResponse listCoupons(@RequestParam String utoken,
                                         @RequestParam(required = false, defaultValue = "0") int status,
                                         @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (status < 0 || status > 3) return MomiaHttpResponse.FAILED("无效的红包状态值");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        PagedList<UserCoupon> userCoupons = couponServiceApi.listUserCoupons(utoken, status, start, Configuration.getInt("PageSize.UserCoupon"));
        return MomiaHttpResponse.SUCCESS(userCoupons);
    }

    @Deprecated
    @RequestMapping(value = "/feed", method = RequestMethod.GET)
    public MomiaHttpResponse listFeeds() {
        return MomiaHttpResponse.SUCCESS(PagedList.EMPTY);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public MomiaHttpResponse getInfo(@RequestParam(value = "uid") long userId, @RequestParam int start) {
        JSONObject infoJson = new JSONObject();

        if (start == 0) {
            User user = userServiceApi.get(userId);
            if (!user.exists()) return MomiaHttpResponse.FAILED("用户不存在");
            infoJson.put("user", completeUserImgs(user));
        }

        infoJson.put("feeds", PagedList.EMPTY);

        return MomiaHttpResponse.SUCCESS(infoJson);
    }

    @RequestMapping(value = "/timeline", method = RequestMethod.GET)
    public MomiaHttpResponse timeline(@RequestParam(value = "uid") long userId, @RequestParam int start) {
        if (userId <= 0) return MomiaHttpResponse.FAILED("无效的用户ID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        JSONObject timelineJson = new JSONObject();

        if (start == 0) {
            User user = userServiceApi.get(userId);
            if (!user.exists()) return MomiaHttpResponse.FAILED("用户不存在");
            timelineJson.put("user", completeUserImgs(user));
        }

        PagedList<TimelineUnit> timeline = courseServiceApi.timelineOfUser(userId, start, Configuration.getInt("PageSize.Timeline"));
        completeTimelineImgs(timeline.getList());
        timelineJson.put("timeline", timeline);

        return MomiaHttpResponse.SUCCESS(timelineJson);
    }

    private List<TimelineUnit> completeTimelineImgs(List<TimelineUnit> list) {
        for (TimelineUnit unit : list) {
            UserCourseComment comment = unit.getComment();
            if (comment != null) completeCourseCommentImgs(comment);
        }

        return list;
    }

    @RequestMapping(value = "/comment/timeline", method = RequestMethod.GET)
    public MomiaHttpResponse commentTimeline(@RequestParam(value = "uid") long userId, @RequestParam int start) {
        if (userId <= 0) return MomiaHttpResponse.FAILED("无效的用户ID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        JSONObject timelineJson = new JSONObject();

        if (start == 0) {
            User user = userServiceApi.get(userId);
            if (!user.exists()) return MomiaHttpResponse.FAILED("用户不存在");
            timelineJson.put("user", completeUserImgs(user));
        }

        PagedList<TimelineUnit> timeline = courseServiceApi.commentTimelineOfUser(userId, start, Configuration.getInt("PageSize.Timeline"));
        completeTimelineImgs(timeline.getList());
        timelineJson.put("timeline", timeline);

        return MomiaHttpResponse.SUCCESS(timelineJson);
    }
}
