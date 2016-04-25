package cn.momia.mapi.api.admin;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.OrderServiceApi;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.user.SmsServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.mapi.api.AbstractApi;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO Admin 相关的内容从MAPI中分离出去
 */
@RestController
@RequestMapping("/admin")
public class AdminApi extends AbstractApi {
    private static final long SYSTEM_PUSH_USERID = 10000;

    @Autowired private ImServiceApi imServiceApi;
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private OrderServiceApi orderServiceApi;
    @Autowired private SmsServiceApi smsServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/im/group", method = RequestMethod.POST)
    public MomiaHttpResponse createGroup(@RequestParam(value = "coid") long courseId,
                                         @RequestParam(value = "sid") long courseSkuId,
                                         @RequestParam(value = "tids", required = false, defaultValue = "") String teachers,
                                         @RequestParam(value = "name") String groupName) {
        if (courseId <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (courseSkuId <= 0) return MomiaHttpResponse.FAILED("无效的场次ID");
        if (StringUtils.isBlank(groupName)) return MomiaHttpResponse.FAILED("无效的群组名称");

        Set<Long> teacherUserIds = new HashSet<Long>();
        for (String teacher : Splitter.on(",").trimResults().omitEmptyStrings().split(teachers)) {
            teacherUserIds.add(Long.valueOf(teacher));
        }

        if (teacherUserIds.isEmpty()) {
            teacherUserIds.add(SYSTEM_PUSH_USERID);
        }

        return MomiaHttpResponse.SUCCESS(imServiceApi.createGroup(courseId, courseSkuId, teacherUserIds, groupName));
    }

    @RequestMapping(value = "/im/group/join", method = RequestMethod.POST)
    public MomiaHttpResponse joinGroup(@RequestParam(value = "uid") long userId,
                                       @RequestParam(value = "coid") long courseId,
                                       @RequestParam(value = "sid") long courseSkuId) {
        if (userId <= 0) return MomiaHttpResponse.FAILED("无效的用户ID");
        if (courseId <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (courseSkuId <= 0) return MomiaHttpResponse.FAILED("无效的场次ID");

        User user = userServiceApi.get(userId);
        return MomiaHttpResponse.SUCCESS(imServiceApi.joinGroup(userId, courseId, courseSkuId, user.isTeacher()));
    }

    @RequestMapping(value = "/im/group/leave", method = RequestMethod.POST)
    public MomiaHttpResponse leaveGroup(@RequestParam(value = "uid") long userId,
                                        @RequestParam(value = "coid") long courseId,
                                        @RequestParam(value = "sid") long courseSkuId) {
        if (userId <= 0) return MomiaHttpResponse.FAILED("无效的用户ID");
        if (courseId <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (courseSkuId <= 0) return MomiaHttpResponse.FAILED("无效的场次ID");

        return MomiaHttpResponse.SUCCESS(imServiceApi.leaveGroup(userId, courseId, courseSkuId));
    }

    @RequestMapping(value = "/course/booking/batch", method = RequestMethod.POST)
    public MomiaHttpResponse batchBooking(@RequestParam String uids,
                                          @RequestParam(value = "coid") long courseId,
                                          @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(uids)) return MomiaHttpResponse.FAILED("用户ID不能为空");
        if (courseId <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (courseSkuId <= 0) return MomiaHttpResponse.FAILED("无效的场次ID");

        Collection<Long> userIds = MomiaUtil.splitDistinctLongs(uids);
        List<Long> failedUserIds = courseServiceApi.batchBooking(userIds, courseId, courseSkuId);
        for (long userId : userIds) {
            if (!failedUserIds.contains(userId)) imServiceApi.joinGroup(userId, courseId, courseSkuId, false);
        }

        return MomiaHttpResponse.SUCCESS(failedUserIds);
    }

    @RequestMapping(value = "/course/cancel/batch", method = RequestMethod.POST)
    public MomiaHttpResponse batchCancel(@RequestParam(required = false, defaultValue = "") String uids,
                                         @RequestParam(value = "coid") long courseId,
                                         @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(uids)) return MomiaHttpResponse.FAILED("用户ID不能为空");
        if (courseId <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (courseSkuId <= 0) return MomiaHttpResponse.FAILED("无效的场次ID");

        Map<Long, Long> successfulPackageUsers = courseServiceApi.batchCancel(MomiaUtil.splitDistinctLongs(uids), courseId, courseSkuId);
        if (!successfulPackageUsers.isEmpty()) {
            for (Map.Entry<Long, Long> entry : successfulPackageUsers.entrySet()) {
                orderServiceApi.extendPackageTime(entry.getKey(), 1);
                imServiceApi.leaveGroup(entry.getValue(), courseId, courseSkuId);
            }
        }

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/course/sku/cancel", method = RequestMethod.POST)
    public MomiaHttpResponse skuCancel(@RequestParam(value = "coid") long courseId, @RequestParam(value = "sid") long courseSkuId) {
        if (courseId <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (courseSkuId <= 0) return MomiaHttpResponse.FAILED("无效的场次ID");

        Map<Long, Long> successfulPackageUsers = courseServiceApi.skuCancel(courseId, courseSkuId);
        if (!successfulPackageUsers.isEmpty()) {
            for (Map.Entry<Long, Long> entry : successfulPackageUsers.entrySet()) {
                orderServiceApi.extendPackageTime(entry.getKey(), 1);
                imServiceApi.leaveGroup(entry.getValue(), courseId, courseSkuId);
            }
        }

        return MomiaHttpResponse.SUCCESS(Sets.newHashSet(successfulPackageUsers.values()));
    }

    @RequestMapping(value = "/package/time/extend", method = RequestMethod.POST)
    public MomiaHttpResponse extendPackageTime(@RequestParam(value = "pid") long packageId, @RequestParam int time) {
        if (packageId <= 0) return MomiaHttpResponse.FAILED("无效的课程包ID");
        if (time <= 0) return MomiaHttpResponse.FAILED("无效的延长时间");
        return MomiaHttpResponse.SUCCESS(orderServiceApi.extendPackageTime(packageId, time));
    }

    @RequestMapping(value = "/push", method = RequestMethod.POST)
    public MomiaHttpResponse push(@RequestParam(value = "uid") long userId,
                                  @RequestParam String content,
                                  @RequestParam(required = false, defaultValue = "") String extra) {
        if (userId <= 0) return MomiaHttpResponse.FAILED("无效的用户ID");
        if (StringUtils.isBlank(content)) return MomiaHttpResponse.FAILED("推送内容不能为空");
        return MomiaHttpResponse.SUCCESS(imServiceApi.push(userId, content, extra));
    }

    @RequestMapping(value = "/push/batch", method = RequestMethod.POST)
    public MomiaHttpResponse pushBatch(@RequestParam(value = "uids") String uids,
                                       @RequestParam String content,
                                       @RequestParam(required = false, defaultValue = "") String extra) {
        if (StringUtils.isBlank(uids)) return MomiaHttpResponse.FAILED("用户ID不能为空");
        if (StringUtils.isBlank(content)) return MomiaHttpResponse.FAILED("推送内容不能为空");

        Set<Long> userIds = new HashSet<Long>();
        for (String userId : Splitter.on(",").omitEmptyStrings().trimResults().split(uids)) {
            userIds.add(Long.valueOf(userId));
        }

        return MomiaHttpResponse.SUCCESS(imServiceApi.pushBatch(userIds, content, extra));
    }

    @RequestMapping(value = "/sms/notify", method = RequestMethod.POST)
    public MomiaHttpResponse smsNotify(@RequestParam String mobile, @RequestParam String message) {
        if (MomiaUtil.isInvalidMobile(mobile)) return MomiaHttpResponse.FAILED("无效的手机号码");
        if (StringUtils.isBlank(message)) return MomiaHttpResponse.FAILED("通知内容不能为空");
        return MomiaHttpResponse.SUCCESS(smsServiceApi.notify(mobile, message));
    }

    @RequestMapping(value = "/refund/check", method = RequestMethod.POST)
    public MomiaHttpResponse checkRefund(@RequestParam(value = "oid") long orderId) {
        if (orderId <= 0) return MomiaHttpResponse.FAILED("无效的订单ID");
        return MomiaHttpResponse.SUCCESS(orderServiceApi.checkRefundOrder(orderId));
    }
}
