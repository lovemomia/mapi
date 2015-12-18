package cn.momia.mapi.api;

import cn.momia.api.course.dto.Course;
import cn.momia.api.course.dto.Subject;
import cn.momia.api.course.dto.UserCourseComment;
import cn.momia.api.feed.dto.UserFeed;
import cn.momia.api.user.dto.Child;
import cn.momia.api.user.dto.User;
import cn.momia.common.client.ClientType;
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
    protected int getClientType(HttpServletRequest request) {
        return StringUtils.isBlank(request.getParameter("terminal")) ? ClientType.WAP : ClientType.APP;
    }

    protected String buildAction(String uri, int clientType) {
        if (ClientType.isApp(clientType)) {
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
            completedImgs.add(ImageFile.url(img));
        }

        return completedImgs;
    }

    protected List<String> completeLargeImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(ImageFile.largeUrl(img));
        }

        return completedImgs;
    }

    protected List<String> completeMiddleImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(ImageFile.middleUrl(img));
        }

        return completedImgs;
    }

    protected List<String> completeSmallImgs(List<String> imgs) {
        if (imgs == null) return null;

        List<String> completedImgs = new ArrayList<String>();
        for (String img : imgs) {
            completedImgs.add(ImageFile.smallUrl(img));
        }

        return completedImgs;
    }

    protected void completeLargeImg(Subject subject) {
        subject.setCover(ImageFile.largeUrl(subject.getCover()));
        subject.setImgs(completeLargeImgs(subject.getImgs()));
    }

    protected void completeLargeImg(Course course) {
        course.setCover(ImageFile.largeUrl(course.getCover()));
        course.setImgs(completeLargeImgs(course.getImgs()));
        completeCourseBookImgs(course.getBook());
    }

    private void completeCourseBookImgs(JSONObject book) {
        if (book == null) return;

        List<String> imgs = new ArrayList<String>();
        List<String> largeImgs = new ArrayList<String>();
        JSONArray imgsJson = book.getJSONArray("imgs");
        for (int i = 0; i < imgsJson.size(); i++) {
            String img = imgsJson.getString(i);
            imgs.add(ImageFile.middleUrl(img));
            largeImgs.add(ImageFile.url(img));
        }

        book.put("imgs", imgs);
        book.put("largeImgs", largeImgs);
    }

    protected void processCourses(List<? extends Course> courses) {
        for (Course course : courses) {
            course.setCover(ImageFile.middleUrl(course.getCover()));
        }
    }

    protected void processCourseComments(List<UserCourseComment> comments) {
        for (UserCourseComment comment : comments) {
            comment.setAvatar(ImageFile.smallUrl(comment.getAvatar()));
            List<String> imgs = comment.getImgs();
            comment.setImgs(completeSmallImgs(imgs));
            comment.setLargeImgs(completeLargeImgs(imgs));
        }
    }

    protected void processFeeds(List<UserFeed> feeds) {
        for (UserFeed feed : feeds) {
            processFeed(feed);
        }
    }

    protected void processFeed(UserFeed feed) {
        List<String> imgs = feed.getImgs();
        feed.setImgs(completeMiddleImgs(imgs));
        feed.setLargeImgs(completeLargeImgs(imgs));
        feed.setAvatar(ImageFile.smallUrl(feed.getAvatar()));
    }

    protected List<User> processUsers(List<User> users) {
        for (User user : users) {
            processUser(user);
        }

        return users;
    }

    protected User processUser(User user) {
        user.setAvatar(ImageFile.smallUrl(user.getAvatar()));
        if (user.getCover() != null) user.setCover(ImageFile.largeUrl(user.getCover()));
        if (user.getChildren() != null) processChildren(user.getChildren());

        return user;
    }

    protected List<Child> processChildren(List<Child> children) {
        for (Child child : children) {
            processChild(child);
        }

        return children;
    }

    protected Child processChild(Child child) {
        child.setAvatar(ImageFile.smallUrl(child.getAvatar()));
        return child;
    }
}
