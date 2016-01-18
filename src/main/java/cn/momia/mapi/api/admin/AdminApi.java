package cn.momia.mapi.api.admin;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.OrderServiceApi;
import cn.momia.api.im.ImServiceApi;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(value = "/im/group", method = RequestMethod.POST)
    public MomiaHttpResponse createGroup(@RequestParam(value = "coid") long courseId,
                                         @RequestParam(value = "sid") long courseSkuId,
                                         @RequestParam(value = "tids", required = false, defaultValue = "") String teachers,
                                         @RequestParam(value = "name") String groupName) {
        if (courseId <= 0 || courseSkuId <= 0 || StringUtils.isBlank(groupName)) return MomiaHttpResponse.BAD_REQUEST;

        Set<Long> teacherUserIds = new HashSet<Long>();
        for (String teacher : Splitter.on(",").trimResults().omitEmptyStrings().split(teachers)) {
            teacherUserIds.add(Long.valueOf(teacher));
        }

        if (teacherUserIds.isEmpty()) {
            teacherUserIds.add(SYSTEM_PUSH_USERID);
        }

        return MomiaHttpResponse.SUCCESS(imServiceApi.createGroup(courseId, courseSkuId, teacherUserIds, groupName));
    }

    @RequestMapping(value = "/course/booking/batch", method = RequestMethod.POST)
    public MomiaHttpResponse batchBooking(@RequestParam String uids,
                                          @RequestParam(value = "coid") long courseId,
                                          @RequestParam(value = "sid") long skuId) {
        Set<Long> userIds = new HashSet<Long>();
        for (String userId : Splitter.on(",").omitEmptyStrings().trimResults().split(uids)) {
            userIds.add(Long.valueOf(userId));
        }

        List<Long> failedUserIds = courseServiceApi.batchBooking(userIds, courseId, skuId);
        for (long userId : userIds) {
            if (!failedUserIds.contains(userId)) imServiceApi.joinGroup(userId, courseId, skuId);
        }

        return MomiaHttpResponse.SUCCESS(failedUserIds);
    }

    @RequestMapping(value = "/course/cancel/batch", method = RequestMethod.POST)
    public MomiaHttpResponse batchCancel(@RequestParam(required = false, defaultValue = "") String uids,
                                         @RequestParam(value = "coid") long courseId,
                                         @RequestParam(value = "sid") long skuId) {
        Set<Long> userIds = new HashSet<Long>();
        for (String userId : Splitter.on(",").omitEmptyStrings().trimResults().split(uids)) {
            userIds.add(Long.valueOf(userId));
        }

        Map<Long, Long> successfulPackageUsers = courseServiceApi.batchCancel(userIds, courseId, skuId);
        if (!successfulPackageUsers.isEmpty()) {
            for (Map.Entry<Long, Long> entry : successfulPackageUsers.entrySet()) {
                orderServiceApi.extendPackageTime(entry.getKey(), 1);
                imServiceApi.leaveGroup(entry.getValue(), courseId, skuId);
            }
        }

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/package/time/extend", method = RequestMethod.POST)
    public MomiaHttpResponse extendPackageTime(@RequestParam(value = "pid") long packageId, @RequestParam int time) {
        if (packageId <= 0 || time <= 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(orderServiceApi.extendPackageTime(packageId, time));
    }

    @RequestMapping(value = "/push", method = RequestMethod.POST)
    public MomiaHttpResponse push(@RequestParam(value = "uid") long userId,
                                  @RequestParam String content,
                                  @RequestParam(required = false, defaultValue = "") String extra) {
        if (userId <= 0 || StringUtils.isBlank(content)) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(imServiceApi.push(userId, content, extra));
    }
}
