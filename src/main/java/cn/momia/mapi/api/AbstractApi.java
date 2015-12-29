package cn.momia.mapi.api;

import cn.momia.api.course.dto.Course;
import cn.momia.api.course.dto.Subject;
import cn.momia.api.course.dto.Teacher;
import cn.momia.api.course.dto.UserCourseComment;
import cn.momia.api.user.dto.Child;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.platform.Platform;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.common.webapp.ctrl.BaseController;
import cn.momia.image.api.ImageFile;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractApi extends BaseController {
    protected int getPlatform(HttpServletRequest request) {
        return StringUtils.isBlank(request.getParameter("terminal")) ? Platform.WAP : Platform.APP;
    }

    protected String getVersion(HttpServletRequest request) {
        String version = request.getParameter("v");
        return StringUtils.isBlank(version) ? "" : version;
    }

    protected String buildAction(String uri, int platform) {
        if (Platform.isApp(platform)) {
            if (uri.startsWith("http")) return Configuration.getString("AppConf.Name") + "://web?url=" + URLEncoder.encode(uri);
            return Configuration.getString("AppConf.Name") + "://" + uri;
        }

        return buildFullUrl(uri);
    }

    private String buildFullUrl(String uri) {
        if (uri.startsWith("http")) return uri;

        if (!uri.startsWith("/")) uri = "/" + uri;
        return uri;
    }

    protected List<String> completeImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(completeImg(img));
        }

        return completedImgs;
    }

    protected String completeImg(String img) {
        return ImageFile.url(img);
    }

    protected List<String> completeLargeImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(completeLargeImg(img));
        }

        return completedImgs;
    }

    protected String completeLargeImg(String img) {
        return ImageFile.largeUrl(img);
    }

    protected List<String> completeMiddleImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(completeMiddleImg(img));
        }

        return completedImgs;
    }

    protected String completeMiddleImg(String img) {
        return ImageFile.middleUrl(img);
    }

    protected List<String> completeSmallImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(completeSmallImg(img));
        }

        return completedImgs;
    }

    protected String completeSmallImg(String img) {
        return ImageFile.smallUrl(img);
    }

    protected Subject completeLargeImg(Subject subject) {
        subject.setCover(completeLargeImg(subject.getCover()));
        subject.setImgs(completeLargeImgs(subject.getImgs()));

        return subject;
    }

    protected Course completeLargeImg(Course course) {
        course.setCover(completeLargeImg(course.getCover()));
        course.setImgs(completeLargeImgs(course.getImgs()));
        course.setBook(completeCourseBookImgs(course.getBook()));

        return course;
    }

    private JSONObject completeCourseBookImgs(JSONObject book) {
        if (book == null) return null;

        List<String> imgs = new ArrayList<String>();
        List<String> largeImgs = new ArrayList<String>();
        JSONArray imgsJson = book.getJSONArray("imgs");
        for (int i = 0; i < imgsJson.size(); i++) {
            String img = imgsJson.getString(i);
            imgs.add(completeMiddleImg(img));
            largeImgs.add(completeImg(img));
        }

        book.put("imgs", imgs);
        book.put("largeImgs", largeImgs);

        return book;
    }

    protected List<? extends Course> completeLargeCoursesImgs(List<? extends Course> courses) {
        for (Course course : courses) {
            course.setCover(completeLargeImg(course.getCover()));
        }

        return courses;
    }

    protected List<? extends Course> completeMiddleCoursesImgs(List<? extends Course> courses) {
        for (Course course : courses) {
            course.setCover(completeMiddleImg(course.getCover()));
        }

        return courses;
    }

    protected List<UserCourseComment> completeCourseCommentsImgs(List<UserCourseComment> comments) {
        for (UserCourseComment comment : comments) {
            completeCourseCommentImgs(comment);
        }

        return comments;
    }

    protected UserCourseComment completeCourseCommentImgs(UserCourseComment comment) {
        comment.setAvatar(completeSmallImg(comment.getAvatar()));
        List<String> imgs = comment.getImgs();
        comment.setImgs(completeSmallImgs(imgs));
        comment.setLargeImgs(completeLargeImgs(imgs));

        return comment;
    }

    protected List<Teacher> completeTeachersImgs(List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            teacher.setAvatar(completeSmallImg(teacher.getAvatar()));
        }

        return teachers;
    }

    protected List<User> completeUsersImgs(List<User> users) {
        for (User user : users) {
            completeUserImgs(user);
        }

        return users;
    }

    protected User completeUserImgs(User user) {
        user.setAvatar(completeSmallImg(user.getAvatar()));
        if (user.getCover() != null) user.setCover(completeLargeImg(user.getCover()));
        if (user.getChildren() != null) completeChildrenImgs(user.getChildren());

        return user;
    }

    protected List<Child> completeChildrenImgs(List<Child> children) {
        for (Child child : children) {
            completeChildImgs(child);
        }

        return children;
    }

    protected Child completeChildImgs(Child child) {
        child.setAvatar(completeSmallImg(child.getAvatar()));
        return child;
    }
}
