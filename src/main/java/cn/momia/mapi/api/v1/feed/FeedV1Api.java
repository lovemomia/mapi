package cn.momia.mapi.api.v1.feed;

import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.FeedCommentDto;
import cn.momia.api.feed.dto.FeedDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/feed")
public class FeedV1Api extends AbstractV1Api {
    @Autowired private FeedServiceApi feedServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse list(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam int start) {
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        long userId = 0;
        if (!StringUtils.isBlank(utoken)) {
            UserDto user = userServiceApi.get(utoken);
            userId = user.getId();
        }

        PagedList<FeedDto> pagedFeeds = feedServiceApi.list(userId, start, Configuration.getInt("PageSize.Feed"));
        processFeeds(pagedFeeds.getList());

        return MomiaHttpResponse.SUCCESS(pagedFeeds);
    }

    private void processFeeds(List<FeedDto> feeds) {
        for (FeedDto feed : feeds) {
            List<String> imgs = feed.getImgs();
            feed.setImgs(completeMiddleImgs(imgs));
            feed.setLargeImgs(completeLargeImgs(imgs));
            feed.setAvatar(ImageFile.smallUrl(feed.getAvatar()));
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse delete(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        if (!feedServiceApi.delete(user.getId(), id)) return MomiaHttpResponse.FAILED("删除Feed失败");

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.GET)
    public MomiaHttpResponse listComments(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<FeedCommentDto> pagedComments = feedServiceApi.listComments(id, start, Configuration.getInt("PageSize.FeedComment"));
        processComments(pagedComments.getList());

        return MomiaHttpResponse.SUCCESS(pagedComments);
    }

    private void processComments(List<FeedCommentDto> comments) {
        for (FeedCommentDto comment : comments) {
            comment.setAvatar(ImageFile.smallUrl(comment.getAvatar()));
        }
    }

    @RequestMapping(value = "/comment/add", method = RequestMethod.POST)
    public MomiaHttpResponse addComment(@RequestParam String utoken, @RequestParam long id, @RequestParam String content) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0 || StringUtils.isBlank(content)) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        feedServiceApi.addComment(user.getId(), id, content);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteComment(@RequestParam String utoken, @RequestParam long id, @RequestParam(value = "cmid") long commentId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0 || commentId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        feedServiceApi.deleteComment(user.getId(), id, commentId);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/star", method = RequestMethod.POST)
    public MomiaHttpResponse star(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        feedServiceApi.star(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unstar", method = RequestMethod.POST)
    public MomiaHttpResponse unstar(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = userServiceApi.get(utoken);
        feedServiceApi.unstar(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }
}
