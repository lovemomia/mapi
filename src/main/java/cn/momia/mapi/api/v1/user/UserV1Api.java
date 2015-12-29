package cn.momia.mapi.api.v1.user;

import cn.momia.api.course.CouponServiceApi;
import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.OrderServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.BookedCourse;
import cn.momia.api.course.dto.Favorite;
import cn.momia.api.course.dto.SubjectPackage;
import cn.momia.api.course.dto.SubjectOrder;
import cn.momia.api.course.dto.TimelineUnit;
import cn.momia.api.course.dto.UserCoupon;
import cn.momia.api.course.dto.UserCourseComment;
import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.Feed;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.user.UserServiceApi;
import cn.momia.common.api.util.SexUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.FeedRelatedApi;
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
public class UserV1Api extends FeedRelatedApi {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private CouponServiceApi couponServiceApi;
    @Autowired private OrderServiceApi orderServiceApi;
    @Autowired private FeedServiceApi feedServiceApi;
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
        if (StringUtils.isBlank(sex) || SexUtil.isInvalid(sex)) return MomiaHttpResponse.FAILED("无效的用户性别");

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

    @RequestMapping(value = "/bookable", method = RequestMethod.GET)
    public MomiaHttpResponse listBookableOrders(@RequestParam String utoken,
                                                @RequestParam(value = "oid", required = false, defaultValue = "0") long orderId,
                                                @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<SubjectPackage> packages = orderServiceApi.listBookable(utoken, orderId, start, Configuration.getInt("PageSize.Subject"));
        for (SubjectPackage orderPackage : packages.getList()) {
            orderPackage.setCover(completeMiddleImg(orderPackage.getCover()));
        }

        return MomiaHttpResponse.SUCCESS(packages);
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public MomiaHttpResponse listOrders(@RequestParam String utoken, @RequestParam int status, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (status <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

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
        if (status < 0 || status > 3 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<UserCoupon> userCoupons = couponServiceApi.listUserCoupons(utoken, status, start, Configuration.getInt("PageSize.UserCoupon"));
        return MomiaHttpResponse.SUCCESS(userCoupons);
    }

    @RequestMapping(value = "/favorite", method = RequestMethod.GET)
    public MomiaHttpResponse listFavorites(@RequestParam String utoken, @RequestParam(defaultValue = "1") int type, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        PagedList<Favorite> favorites;
        switch (type) {
            case Favorite.Type.SUBJECT:
                favorites = subjectServiceApi.listFavorites(user.getId(), start, Configuration.getInt("PageSize.Favorite"));
                completeFavoritesImgs(favorites);
                break;
            default:
                favorites = courseServiceApi.listFavorites(user.getId(), start, Configuration.getInt("PageSize.Favorite"));
                completeFavoritesImgs(favorites);
        }

        return MomiaHttpResponse.SUCCESS(favorites);
    }

    private void completeFavoritesImgs(PagedList<Favorite> favorites) {
        for (Favorite favorite : favorites.getList()) {
            JSONObject ref = favorite.getRef();
            ref.put("cover", completeMiddleImg(ref.getString("cover")));
        }
    }

    @RequestMapping(value = "/feed", method = RequestMethod.GET)
    public MomiaHttpResponse listFeeds(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        PagedList<Feed> pagedFeeds = feedServiceApi.listFeedsOfUser(user.getId(), start, Configuration.getInt("PageSize.Feed"));

        return MomiaHttpResponse.SUCCESS(buildPagedUserFeeds(user.getId(), pagedFeeds));
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public MomiaHttpResponse getInfo(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam(value = "uid") long userId, @RequestParam int start) {
        JSONObject infoJson = new JSONObject();

        if (start == 0) {
            User user = userServiceApi.get(userId);
            if (!user.exists()) return MomiaHttpResponse.FAILED("用户不存在");
            infoJson.put("user", completeUserImgs(user));
        }

        long selfId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        PagedList<Feed> pagedFeeds = feedServiceApi.listFeedsOfUser(userId, start, Configuration.getInt("PageSize.Feed"));

        infoJson.put("feeds", buildPagedUserFeeds(selfId, pagedFeeds));

        return MomiaHttpResponse.SUCCESS(infoJson);
    }

    @RequestMapping(value = "/timeline", method = RequestMethod.GET)
    public MomiaHttpResponse timeline(@RequestParam(value = "uid") long userId, @RequestParam int start) {
        if (userId <=0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

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
        if (userId <=0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

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
