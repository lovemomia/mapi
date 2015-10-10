package cn.momia.mapi.api.v1;

import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.api.user.dto.UserDto;

public class AbstractV1Api extends AbstractApi {
    protected UserDto processUser(UserDto user) {
        user.setAvatar(ImageFile.smallUrl(user.getAvatar()));
        return user;
    }
}
