package cn.momia.mapi.api.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.course.BookedCourse;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.course.CourseSku;
import cn.momia.api.course.dto.course.DatedCourseSkus;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.poi.PoiServiceApi;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");

        Course course = completeLargeImg(courseServiceApi.get(id, pos));
        JSONObject courseJson = (JSONObject) JSON.toJSON(course);

        PagedList<Integer> pagedTeacherIds = courseServiceApi.teacherIds(id, 0, Configuration.getInt("PageSize.CourseTeacher"));
        List<Teacher> teachers = completeTeachersImgs(teacherServiceApi.list(pagedTeacherIds.getList()));
        if (!teachers.isEmpty()) courseJson.put("teachers", teachers);

        return MomiaHttpResponse.SUCCESS(courseJson);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public MomiaHttpResponse detail(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        return MomiaHttpResponse.SUCCESS(courseServiceApi.detail(id));
    }

    @RequestMapping(value = "/book", method = RequestMethod.GET)
    public MomiaHttpResponse book(@RequestParam long id, @RequestParam int start) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        PagedList<String> book = courseServiceApi.book(id, start, Configuration.getInt("PageSize.BookImg"));
        book.setList(completeLargeImgs(book.getList()));

        return MomiaHttpResponse.SUCCESS(book);
    }

    @RequestMapping(value = "/teacher", method = RequestMethod.GET)
    public MomiaHttpResponse teacher(@RequestParam long id, @RequestParam int start) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        PagedList<Integer> pagedTeacherIds = courseServiceApi.teacherIds(id, start, Configuration.getInt("PageSize.Teacher"));
        List<Teacher> teachers = completeTeachersImgs(teacherServiceApi.list(pagedTeacherIds.getList()));

        PagedList<Teacher> pagedTeachers = new PagedList<Teacher>();
        pagedTeachers.setTotalCount(pagedTeacherIds.getTotalCount());
        pagedTeachers.setNextIndex(pagedTeacherIds.getNextIndex());
        pagedTeachers.setList(teachers);

        return MomiaHttpResponse.SUCCESS(pagedTeachers);
    }

    @RequestMapping(value = "/sku/week", method = RequestMethod.GET)
    public MomiaHttpResponse listWeekSkus(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
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
    public MomiaHttpResponse listMonthSkus(@RequestParam long id, @RequestParam(required = false, defaultValue = "0") int month) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (month <= 0 || month > 12) return MomiaHttpResponse.SUCCESS(filterClosedSkus(courseServiceApi.listSkus(id)));
        return MomiaHttpResponse.SUCCESS(filterClosedSkus(courseServiceApi.listMonthSkus(id, month)));
    }

    @RequestMapping(value = "/sku/week/notend", method = RequestMethod.GET)
    public MomiaHttpResponse listNotEndWeekSkus(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listWeekSkus(id));
    }

    @RequestMapping(value = "/sku/month/notend", method = RequestMethod.GET)
    public MomiaHttpResponse lisNotEndtMonthSkus(@RequestParam long id, @RequestParam(required = false, defaultValue = "0") int month) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (month <= 0 || month > 12) return MomiaHttpResponse.SUCCESS(courseServiceApi.listSkus(id));
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listMonthSkus(id, month));
    }

    @RequestMapping(value = "/sku/week/bookable", method = RequestMethod.GET)
    public MomiaHttpResponse listBookableWeekSkus(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
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
    public MomiaHttpResponse listBookableMonthSkus(@RequestParam long id, @RequestParam(required = false, defaultValue = "0") int month) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        if (month <= 0 || month > 12) return MomiaHttpResponse.SUCCESS(filterUnbookableSkus(courseServiceApi.listSkus(id)));
        return MomiaHttpResponse.SUCCESS(filterUnbookableSkus(courseServiceApi.listMonthSkus(id, month)));
    }

    @RequestMapping(value = "/booking", method = RequestMethod.POST)
    public MomiaHttpResponse booking(@RequestParam String utoken,
                                     @RequestParam(value = "cid", required = false, defaultValue = "0") long childId,
                                     @RequestParam(value = "pid") long packageId,
                                     @RequestParam(value = "sid") long skuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (packageId <= 0) return MomiaHttpResponse.FAILED("无效的课程包ID");
        if (skuId <= 0) return MomiaHttpResponse.FAILED("无效的场次ID");

        User user = userServiceApi.get(utoken);
        BookedCourse bookedCourse = courseServiceApi.booking(utoken, childId, packageId, skuId);
        if (bookedCourse.getParentId() > 0 && bookedCourse.getParentCourseSkuId() > 0) {
            imServiceApi.joinGroup(user.getId(), bookedCourse.getParentId(), bookedCourse.getParentCourseSkuId(), false);
        } else {
            imServiceApi.joinGroup(user.getId(), bookedCourse.getId(), bookedCourse.getCourseSkuId(), false);
        }

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public MomiaHttpResponse cancel(@RequestParam String utoken, @RequestParam(value = "bid") long bookingId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (bookingId <= 0) return MomiaHttpResponse.FAILED("无效的预约ID");

        User user = userServiceApi.get(utoken);
        BookedCourse bookedCourse = courseServiceApi.cancel(utoken, bookingId);
        if (bookedCourse.getParentId() > 0 && bookedCourse.getParentCourseSkuId() > 0) {
            imServiceApi.leaveGroup(user.getId(), bookedCourse.getParentId(), bookedCourse.getParentCourseSkuId());
        } else {
            imServiceApi.leaveGroup(user.getId(), bookedCourse.getId(), bookedCourse.getCourseSkuId());
        }

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public MomiaHttpResponse comment(String utoken, String comment) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(comment)) return MomiaHttpResponse.FAILED("评论内容不能为空");

        User user = userServiceApi.get(utoken);
        JSONObject commentJson = JSON.parseObject(comment);
        commentJson.put("userId", user.getId());

        if (!courseServiceApi.comment(commentJson)) return MomiaHttpResponse.FAILED("发表评论失败");
        return MomiaHttpResponse.SUCCESS;
    }
    @RequestMapping(value = "/comment/list", method = RequestMethod.GET)
    public MomiaHttpResponse listComment(@RequestParam long id, @RequestParam int start) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");
        
        PagedList<UserCourseComment> pagedComments = courseServiceApi.queryCommentsByCourse(id, start, Configuration.getInt("PageSize.CourseComment"));
        completeCourseCommentsImgs(pagedComments.getList());

        return MomiaHttpResponse.SUCCESS(pagedComments);
    }
}
