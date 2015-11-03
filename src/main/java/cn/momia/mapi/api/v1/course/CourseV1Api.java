package cn.momia.mapi.api.v1.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseBookDto;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.InstitutionDto;
import cn.momia.api.course.dto.TeacherDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSON;
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
@RequestMapping(value = "/v1/course")
public class CourseV1Api extends AbstractV1Api {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(required = false, defaultValue = "") String utoken,
                                 @RequestParam long id,
                                 @RequestParam(required = false, defaultValue = "") String pos) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        CourseDto course = processCourse(courseServiceApi.get(id, pos));
        JSONObject courseJson = (JSONObject) JSON.toJSON(course);
        if (!StringUtils.isBlank(utoken)) {
            UserDto user = userServiceApi.get(utoken);
            courseJson.put("favored", courseServiceApi.isFavored(user.getId(), id));
        }

        List<TeacherDto> teachers = processTeachers(courseServiceApi.queryTeachers(id, 0, Configuration.getInt("PageSize.CourseTeacher")).getList());
        if (!teachers.isEmpty()) courseJson.put("teachers", teachers);

        return MomiaHttpResponse.SUCCESS(courseJson);
    }

    protected CourseDto processCourse(CourseDto course) {
        course.setCover(ImageFile.largeUrl(course.getCover()));

        processLargeImgs(course.getImgs());
        processCourseBook(course.getBook());

        return course;
    }

    private CourseBookDto processCourseBook(CourseBookDto book) {
        if (book == null) return null;

        List<String> imgs = new ArrayList<String>();
        List<String> largeImgs = new ArrayList<String>();
        for (String img : book.getImgs()) {
            imgs.add(ImageFile.smallUrl(img));
            largeImgs.add(ImageFile.url(img));
        }

        book.setImgs(imgs);
        book.setLargeImgs(largeImgs);

        return book;
    }

    private List<TeacherDto> processTeachers(List<TeacherDto> teachers) {
        for (TeacherDto teacher : teachers) {
            teacher.setAvatar(ImageFile.smallUrl(teacher.getAvatar()));
        }

        return teachers;
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
        processImgs(book.getList());

        return MomiaHttpResponse.SUCCESS(book);
    }

    @RequestMapping(value = "/teacher", method = RequestMethod.GET)
    public MomiaHttpResponse teacher(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<TeacherDto> teachers = courseServiceApi.queryTeachers(id, start, Configuration.getInt("PageSize.Teacher"));
        processTeachers(teachers.getList());

        return MomiaHttpResponse.SUCCESS(teachers);
    }

    @RequestMapping(value = "/institution", method = RequestMethod.GET)
    public MomiaHttpResponse institution(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        InstitutionDto institution = courseServiceApi.institution(id);
        institution.setCover(ImageFile.largeUrl(institution.getCover()));

        return MomiaHttpResponse.SUCCESS(institution);
    }

    @RequestMapping(value = "/sku/week", method = RequestMethod.GET)
    public MomiaHttpResponse listWeekSkus(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listWeekSkus(id));
    }

    @RequestMapping(value = "/sku/month", method = RequestMethod.GET)
    public MomiaHttpResponse listWeekSkus(@RequestParam long id, @RequestParam int month) {
        if (id <= 0 || month <= 0 || month > 12) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listMonthSkus(id, month));
    }

    @RequestMapping(value = "/booking", method = RequestMethod.POST)
    public MomiaHttpResponse booking(@RequestParam String utoken,
                                     @RequestParam(value = "pid") long packageId,
                                     @RequestParam(value = "sid") long skuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (packageId <= 0 || skuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        if (!courseServiceApi.booking(utoken, packageId, skuId)) return MomiaHttpResponse.FAILED("选课失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public MomiaHttpResponse cancel(@RequestParam String utoken, @RequestParam(value = "bid") long bookingId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (bookingId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        if (!courseServiceApi.cancel(utoken, bookingId)) return MomiaHttpResponse.FAILED("取消选课失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public MomiaHttpResponse comment(String utoken, String comment) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(comment)) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        JSONObject commentJson = JSON.parseObject(comment);
        commentJson.put("userId", user.getId());

        if (!courseServiceApi.comment(commentJson)) return MomiaHttpResponse.FAILED("发表评论失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/favor", method = RequestMethod.POST)
    public MomiaHttpResponse favor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        if (!courseServiceApi.favor(user.getId(), id)) return MomiaHttpResponse.FAILED("添加收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unfavor", method = RequestMethod.POST)
    public MomiaHttpResponse unfavor(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        if (!courseServiceApi.unfavor(user.getId(), id)) return MomiaHttpResponse.FAILED("取消收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }
}
