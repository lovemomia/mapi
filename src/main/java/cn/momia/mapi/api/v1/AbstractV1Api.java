package cn.momia.mapi.api.v1;

import cn.momia.api.course.dto.CourseBookDto;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.OrderDto;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.api.course.dto.TeacherDto;
import cn.momia.api.user.dto.UserChildDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

public class AbstractV1Api extends AbstractApi {
    protected List<String> processLargeImgs(List<String> imgs) {
        if (imgs == null) return imgs;

        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.largeUrl(imgs.get(i)));
        }

        return imgs;
    }

    protected List<String> processMiddleImgs(List<String> imgs) {
        if (imgs == null) return imgs;

        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.middleUrl(imgs.get(i)));
        }

        return imgs;
    }

    protected List<String> processSmallImgs(List<String> imgs) {
        if (imgs == null) return imgs;

        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.smallUrl(imgs.get(i)));
        }

        return imgs;
    }

    protected PagedList<SubjectDto> processPagedSubjects(PagedList<SubjectDto> subjects) {
        for (SubjectDto subject : subjects.getList()) {
            subject.setCover(ImageFile.largeUrl(subject.getCover()));
        }

        return subjects;
    }

    protected SubjectDto processSubject(SubjectDto subject) {
        subject.setCover(ImageFile.largeUrl(subject.getCover()));
        List<String> imgs = subject.getImgs();
        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.largeUrl(imgs.get(i)));
        }

        return subject;
    }

    protected PagedList<CourseDto> processPagedCourses(PagedList<CourseDto> courses) {
        for (CourseDto course : courses.getList()) {
            processCourse(course);
        }

        return courses;
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
            largeImgs.add(ImageFile.largeUrl(img));
        }

        book.setImgs(imgs);
        book.setLargeImgs(largeImgs);

        return book;
    }

    protected List<TeacherDto> processTeachers(List<TeacherDto> teachers) {
        for (TeacherDto teacher : teachers) {
            teacher.setAvatar(ImageFile.smallUrl(teacher.getAvatar()));
        }

        return teachers;
    }

    protected PagedList<OrderDto> processPagedOrders(PagedList<OrderDto> orders) {
        for (OrderDto order : orders.getList()) {
            order.setCover(ImageFile.middleUrl(order.getCover()));
        }

        return orders;
    }

    protected UserDto processUser(UserDto user) {
        user.setAvatar(ImageFile.smallUrl(user.getAvatar()));
        processChildren(user.getChildren());

        return user;
    }

    protected List<UserChildDto> processChildren(List<UserChildDto> children) {
        for (UserChildDto child : children) {
            processChild(child);
        }

        return children;
    }

    protected UserChildDto processChild(UserChildDto child) {
        child.setAvatar(ImageFile.smallUrl(child.getAvatar()));
        return child;
    }
}
