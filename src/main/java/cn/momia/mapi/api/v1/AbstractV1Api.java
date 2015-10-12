package cn.momia.mapi.api.v1;

import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.user.dto.UserDto;

import java.util.List;

public class AbstractV1Api extends AbstractApi {
    protected PagedList<CourseDto> processPagedCourses(PagedList<CourseDto> courses) {
        for (CourseDto course : courses.getList()) {
            course.setCover(ImageFile.largeUrl(course.getCover()));
        }

        return courses;
    }

    protected SubjectDto processSubject(SubjectDto subjectDto) {
        List<String> imgs = subjectDto.getImgs();
        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.largeUrl(imgs.get(i)));
        }

        return subjectDto;
    }

    protected UserDto processUser(UserDto user) {
        user.setAvatar(ImageFile.smallUrl(user.getAvatar()));
        return user;
    }
}
