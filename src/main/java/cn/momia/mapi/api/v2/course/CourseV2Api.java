package cn.momia.mapi.api.v2.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseBookDto;
import cn.momia.api.course.dto.UserCourseComment;
import cn.momia.api.course.dto.CourseDetail;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.Teacher;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v2.AbstractV2Api;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v2/course")
public class CourseV2Api extends AbstractV2Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseV2Api.class);

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
            User user = userServiceApi.get(utoken);
            courseJson.put("favored", courseServiceApi.isFavored(user.getId(), id));
        }

        List<Teacher> teachers = processTeachers(courseServiceApi.teacher(id, 0, Configuration.getInt("PageSize.CourseTeacher")).getList());
        if (!teachers.isEmpty()) courseJson.put("teachers", teachers);

        PagedList<UserCourseComment> pagedComments = courseServiceApi.queryCommentsByCourse(id, 0, 1);
        processCourseComments(pagedComments.getList());
        if (!pagedComments.getList().isEmpty()) courseJson.put("comments", pagedComments);

        try {
            CourseDetail detail = courseServiceApi.detail(id);
            JSONArray detailJson = JSON.parseArray(detail.getDetail());
            for (int i = 0; i < detailJson.size(); i++) {
                JSONObject detailBlockJson = detailJson.getJSONObject(i);
                JSONArray contentJson = detailBlockJson.getJSONArray("content");
                for (int j = 0; j < contentJson.size(); j++) {
                    JSONObject contentBlockJson = contentJson.getJSONObject(j);
                    if (contentBlockJson.containsKey("img")) contentBlockJson.put("img", ImageFile.largeUrl(contentBlockJson.getString("img")));
                }
            }
            courseJson.put("goal", detail.getAbstracts());
            courseJson.put("detail", detailJson);
        } catch (Exception e) {
            LOGGER.warn("invalid course detail: {}", id);
        }

        return MomiaHttpResponse.SUCCESS(courseJson);
    }

    private CourseDto processCourse(CourseDto course) {
        course.setCover(ImageFile.largeUrl(course.getCover()));

        course.setImgs(completeLargeImgs(course.getImgs()));
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

    private List<Teacher> processTeachers(List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            teacher.setAvatar(ImageFile.smallUrl(teacher.getAvatar()));
        }

        return teachers;
    }
}
