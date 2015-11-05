package cn.momia.mapi.api.v1;

import cn.momia.api.feed.dto.FeedDto;
import cn.momia.api.user.dto.ChildDto;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

public class AbstractV1Api extends AbstractApi {
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
