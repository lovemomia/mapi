package cn.momia.mapi.api.v1;

import cn.momia.api.user.dto.ChildDto;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.user.dto.UserDto;

import java.util.List;

public class AbstractV1Api extends AbstractApi {
    protected List<String> processImgs(List<String> imgs) {
        if (imgs == null) return imgs;

        for (int i = 0; i < imgs.size(); i++) {
            imgs.set(i, ImageFile.url(imgs.get(i)));
        }

        return imgs;
    }

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
