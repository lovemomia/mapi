package cn.momia.mapi.api.v1;

import cn.momia.api.course.dto.CourseCommentDto;
import cn.momia.api.feed.dto.FeedDto;
import cn.momia.api.user.dto.ChildDto;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.user.dto.UserDto;

import java.util.List;

public class AbstractV1Api extends AbstractApi {
    protected void processCourseComments(List<CourseCommentDto> comments) {
        for (CourseCommentDto comment : comments) {
            comment.setAvatar(ImageFile.smallUrl(comment.getAvatar()));
            List<String> imgs = comment.getImgs();
            comment.setImgs(completeSmallImgs(imgs));
            comment.setLargeImgs(completeLargeImgs(imgs));
        }
    }

    protected void processFeeds(List<FeedDto> feeds) {
        for (FeedDto feed : feeds) {
            processFeed(feed);
        }
    }

    protected void processFeed(FeedDto feed) {
        List<String> imgs = feed.getImgs();
        feed.setImgs(completeMiddleImgs(imgs));
        feed.setLargeImgs(completeLargeImgs(imgs));
        feed.setAvatar(ImageFile.smallUrl(feed.getAvatar()));
    }

    protected UserDto processUser(UserDto user) {
        user.setAvatar(ImageFile.smallUrl(user.getAvatar()));
        user.setCover(ImageFile.largeUrl(user.getCover()));
        processChildren(user.getChildren());

        return user;
    }

    protected List<ChildDto> processChildren(List<ChildDto> children) {
        for (ChildDto child : children) {
            processChild(child);
        }

        return children;
    }

    protected ChildDto processChild(ChildDto child) {
        child.setAvatar(ImageFile.smallUrl(child.getAvatar()));
        return child;
    }
}
