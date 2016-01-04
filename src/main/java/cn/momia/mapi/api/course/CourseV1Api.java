package cn.momia.mapi.api.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.course.BookedCourse;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.course.CourseSku;
import cn.momia.api.course.dto.course.DatedCourseSkus;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.poi.PoiServiceApi;
import cn.momia.api.poi.dto.Institution;
import cn.momia.api.user.TeacherServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Teacher;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/v1/course")
public class CourseV1Api extends AbstractApi {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private PoiServiceApi poiServiceApi;
    @Autowired private ImServiceApi imServiceApi;
    @Autowired private UserServiceApi userServiceApi;
    @Autowired private TeacherServiceApi teacherServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(required = false, defaultValue = "") String utoken,
                                 @RequestParam long id,
                                 @RequestParam(required = false, defaultValue = "") String pos) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Course course = completeLargeImg(courseServiceApi.get(id, pos));
        JSONObject courseJson = (JSONObject) JSON.toJSON(course);
        if (!StringUtils.isBlank(utoken)) {
            User user = userServiceApi.get(utoken);
            courseJson.put("favored", courseServiceApi.isFavored(user.getId(), id));
        }

        PagedList<Integer> pagedTeacherIds = courseServiceApi.teacherIds(id, 0, Configuration.getInt("PageSize.CourseTeacher"));
        List<Teacher> teachers = completeTeachersImgs(teacherServiceApi.list(pagedTeacherIds.getList()));
        if (!teachers.isEmpty()) courseJson.put("teachers", teachers);

        return MomiaHttpResponse.SUCCESS(courseJson);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public MomiaHttpResponse detail(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(courseServiceApi.detail(id));
    }

    @RequestMapping(value = "/book", method = RequestMethod.GET)
    public MomiaHttpResponse book(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<String> book = courseServiceApi.book(id, start, Configuration.getInt("PageSize.BookImg"));
        book.setList(completeLargeImgs(book.getList()));

        return MomiaHttpResponse.SUCCESS(book);
    }

    @RequestMapping(value = "/teacher", method = RequestMethod.GET)
    public MomiaHttpResponse teacher(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<Integer> pagedTeacherIds = courseServiceApi.teacherIds(id, start, Configuration.getInt("PageSize.Teacher"));
        List<Teacher> teachers = completeTeachersImgs(teacherServiceApi.list(pagedTeacherIds.getList()));

        PagedList<Teacher> pagedTeachers = new PagedList<Teacher>();
        pagedTeachers.setTotalCount(pagedTeacherIds.getTotalCount());
        pagedTeachers.setNextIndex(pagedTeacherIds.getNextIndex());
        pagedTeachers.setList(teachers);

        return MomiaHttpResponse.SUCCESS(pagedTeachers);
    }

    @RequestMapping(value = "/institution", method = RequestMethod.GET)
    public MomiaHttpResponse institution(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        int institutionId = courseServiceApi.getInstitutionId(id);
        Institution institution = poiServiceApi.getInstitution(institutionId);
        if (!institution.exists()) return MomiaHttpResponse.FAILED("机构信息不存在");

        institution.setCover(completeLargeImg(institution.getCover()));

        return MomiaHttpResponse.SUCCESS(institution);
    }

    @RequestMapping(value = "/sku/week", method = RequestMethod.GET)
    public MomiaHttpResponse listWeekSkus(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(filterClosedSkus(courseServiceApi.listWeekSkus(id)));
    }

    private List<DatedCourseSkus> filterClosedSkus(List<DatedCourseSkus> allDatedCourseSkus) {
        List<DatedCourseSkus> result = new ArrayList<DatedCourseSkus>();
        for (DatedCourseSkus datedCourseSkus : allDatedCourseSkus) {
            List<CourseSku> filteredSkus = new ArrayList<CourseSku>();
            for (CourseSku sku : datedCourseSkus.getSkus()) {
                if (!sku.isClosed()) filteredSkus.add(sku);
            }

            if (filteredSkus.size() > 0) {
                DatedCourseSkus newDatedCourseSkus = new DatedCourseSkus();
                newDatedCourseSkus.setDate(datedCourseSkus.getDate());
                newDatedCourseSkus.setSkus(filteredSkus);
                result.add(newDatedCourseSkus);
            }
        }
        return result;
    }

    @RequestMapping(value = "/sku/month", method = RequestMethod.GET)
    public MomiaHttpResponse listMonthSkus(@RequestParam long id, @RequestParam int month) {
        if (id <= 0 || month <= 0 || month > 12) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(filterClosedSkus(courseServiceApi.listMonthSkus(id, month)));
    }

    @RequestMapping(value = "/sku/week/notend", method = RequestMethod.GET)
    public MomiaHttpResponse listNotEndWeekSkus(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listWeekSkus(id));
    }

    @RequestMapping(value = "/sku/month/notend", method = RequestMethod.GET)
    public MomiaHttpResponse lisNotEndtMonthSkus(@RequestParam long id, @RequestParam int month) {
        if (id <= 0 || month <= 0 || month > 12) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listMonthSkus(id, month));
    }

    @RequestMapping(value = "/sku/week/bookable", method = RequestMethod.GET)
    public MomiaHttpResponse listBookableWeekSkus(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(filterUnbookableSkus(courseServiceApi.listWeekSkus(id)));
    }

    private List<DatedCourseSkus> filterUnbookableSkus(List<DatedCourseSkus> allDatedCourseSkus) {
        Date now = new Date();
        List<DatedCourseSkus> result = new ArrayList<DatedCourseSkus>();
        for (DatedCourseSkus datedCourseSkus : allDatedCourseSkus) {
            List<CourseSku> filteredSkus = new ArrayList<CourseSku>();
            for (CourseSku sku : datedCourseSkus.getSkus()) {
                if (sku.isBookable(now)) filteredSkus.add(sku);
            }

            if (filteredSkus.size() > 0) {
                DatedCourseSkus newDatedCourseSkus = new DatedCourseSkus();
                newDatedCourseSkus.setDate(datedCourseSkus.getDate());
                newDatedCourseSkus.setSkus(filteredSkus);
                result.add(newDatedCourseSkus);
            }
        }
        return result;
    }

    @RequestMapping(value = "/sku/month/bookable", method = RequestMethod.GET)
    public MomiaHttpResponse listBookableMonthSkus(@RequestParam long id, @RequestParam int month) {
        if (id <= 0 || month <= 0 || month > 12) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(filterUnbookableSkus(courseServiceApi.listMonthSkus(id, month)));
    }

    @RequestMapping(value = "/booking", method = RequestMethod.POST)
    public MomiaHttpResponse booking(@RequestParam String utoken,
                                     @RequestParam(value = "pid") long packageId,
                                     @RequestParam(value = "sid") long skuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (packageId <= 0 || skuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        BookedCourse bookedCourse = courseServiceApi.booking(utoken, packageId, skuId);
        if (bookedCourse.getParentId() > 0 && bookedCourse.getParentCourseSkuId() > 0) {
            imServiceApi.joinGroup(user.getId(), bookedCourse.getParentId(), bookedCourse.getParentCourseSkuId());
        } else {
            imServiceApi.joinGroup(user.getId(), bookedCourse.getId(), bookedCourse.getCourseSkuId());
        }

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/booking/batch", method = RequestMethod.POST)
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

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public MomiaHttpResponse cancel(@RequestParam String utoken, @RequestParam(value = "bid") long bookingId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (bookingId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        BookedCourse bookedCourse = courseServiceApi.cancel(utoken, bookingId);
        if (bookedCourse.getParentId() > 0 && bookedCourse.getParentCourseSkuId() > 0) {
            imServiceApi.leaveGroup(user.getId(), bookedCourse.getParentId(), bookedCourse.getParentCourseSkuId());
        } else {
            imServiceApi.leaveGroup(user.getId(), bookedCourse.getId(), bookedCourse.getCourseSkuId());
        }

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/cancel/batch", method = RequestMethod.POST)
    public MomiaHttpResponse batchCancel(@RequestParam String uids,
                                         @RequestParam(value = "coid") long courseId,
                                         @RequestParam(value = "sid") long skuId) {
        Set<Long> userIds = new HashSet<Long>();
        for (String userId : Splitter.on(",").omitEmptyStrings().trimResults().split(uids)) {
            userIds.add(Long.valueOf(userId));
        }

        boolean successful = courseServiceApi.batchCancel(userIds, courseId, skuId);
        if (successful) {
            for (long userId : userIds) {
                imServiceApi.leaveGroup(userId, courseId, skuId);
            }
        }

        return MomiaHttpResponse.SUCCESS(successful);
    }

    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public MomiaHttpResponse comment(String utoken, String comment) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(comment)) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        JSONObject commentJson = JSON.parseObject(comment);
        commentJson.put("userId", user.getId());

        if (!courseServiceApi.comment(commentJson)) return MomiaHttpResponse.FAILED("发表评论失败");
        return MomiaHttpResponse.SUCCESS;
    }
    @RequestMapping(value = "/comment/list", method = RequestMethod.GET)
    public MomiaHttpResponse listComment(@RequestParam long id, @RequestParam int start) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;
        PagedList<UserCourseComment> pagedComments = courseServiceApi.queryCommentsByCourse(id, start, Configuration.getInt("PageSize.CourseComment"));
        completeCourseCommentsImgs(pagedComments.getList());

        return MomiaHttpResponse.SUCCESS(pagedComments);
    }

    @RequestMapping(value = "/favor", method = RequestMethod.POST)
    public MomiaHttpResponse favor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        if (!courseServiceApi.favor(user.getId(), id)) return MomiaHttpResponse.FAILED("添加收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unfavor", method = RequestMethod.POST)
    public MomiaHttpResponse unfavor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        if (!courseServiceApi.unfavor(user.getId(), id)) return MomiaHttpResponse.FAILED("取消收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }
}
