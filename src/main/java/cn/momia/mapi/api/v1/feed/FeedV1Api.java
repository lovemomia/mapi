package cn.momia.mapi.api.v1.feed;

import cn.momia.api.feed.dto.FeedCommentDto;
import cn.momia.api.feed.dto.FeedStarDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.FeedDto;
import cn.momia.api.product.ProductServiceApi;
import cn.momia.api.product.dto.ProductDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/feed")
public class FeedV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedV1Api.class);

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    public MomiaHttpResponse follow(@RequestParam String utoken, @RequestParam(value = "fuid") long followedId) {
        if (StringUtils.isBlank(utoken) || followedId < 0) return MomiaHttpResponse.BAD_REQUEST;
        if (!UserServiceApi.USER.get(followedId).exists()) return MomiaHttpResponse.FAILED("关注的用户不存在");

        UserDto user = UserServiceApi.USER.get(utoken);
        FeedServiceApi.FEED.follow(user.getId(), followedId);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse list(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam int start) {
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        long userId = StringUtils.isBlank(utoken) ? 0 : UserServiceApi.USER.get(utoken).getId();
        return MomiaHttpResponse.SUCCESS(processPagedFeeds(FeedServiceApi.FEED.list(userId, start, Configuration.getInt("PageSize.Feed.List"))));
    }

    private PagedList<FeedDto> processPagedFeeds(PagedList<FeedDto> feeds) {
        for (FeedDto feed : feeds.getList()) {
            processFeed(feed);
        }

        return feeds;
    }

    private FeedDto processFeed(FeedDto feed) {
        feed.setAvatar(ImageFile.smallUrl(feed.getAvatar()));
        if (feed.getImgs() != null) {
            for (int i = 0; i < feed.getImgs().size(); i++) {
                feed.getImgs().set(i, ImageFile.middleUrl(feed.getImgs().get(i)));
            }
        }

        return feed;
    }

    @RequestMapping(value = "/topic", method = RequestMethod.GET)
    public MomiaHttpResponse topic(@RequestParam(defaultValue = "") String utoken,
                                   @RequestParam(value = "pid") long productId,
                                   @RequestParam(value = "tid") long topicId,
                                   @RequestParam final int start) {
        if (productId <= 0 || topicId <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject feedTopicJson = new JSONObject();

        if (start == 0) feedTopicJson.put("product", processProduct(ProductServiceApi.PRODUCT.get(productId, ProductDto.Type.BASE), ImageFile.Size.MIDDLE));

        long userId = StringUtils.isBlank(utoken) ? 0 : UserServiceApi.USER.get(utoken).getId();
        feedTopicJson.put("feeds", processPagedFeeds(FeedServiceApi.FEED.listByTopic(userId, topicId, start, Configuration.getInt("PageSize.Feed.List"))));

        return MomiaHttpResponse.SUCCESS(feedTopicJson);
    }

    @RequestMapping(value = "/topic/list", method = RequestMethod.GET)
    public MomiaHttpResponse listTopic(@RequestParam(value = "topictype", defaultValue = "1") int topicType, @RequestParam int start) {
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(FeedServiceApi.FEED.listTopic(topicType, start, Configuration.getInt("PageSize.Feed.Topic")));
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public MomiaHttpResponse add(@RequestParam String utoken, @RequestParam String feed) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(feed)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject feedJson = JSON.parseObject(feed);
        JSONObject baseFeedJson = feedJson.getJSONObject("baseFeed");
        if (baseFeedJson == null) return MomiaHttpResponse.BAD_REQUEST;

        baseFeedJson.put("userId", UserServiceApi.USER.get(utoken).getId());
        FeedServiceApi.FEED.add(feedJson);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public MomiaHttpResponse detail(@RequestParam(defaultValue = "") String utoken, @RequestParam long id,
                                    @RequestParam(value = "pid", required = false, defaultValue = "0") long productId) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = UserServiceApi.USER.get(utoken);
        FeedDto feed = FeedServiceApi.FEED.get(user.getId(), id);
        PagedList<FeedStarDto> stars = processPagedFeedStars(FeedServiceApi.FEED.listStars(id, 0, Configuration.getInt("PageSize.Feed.Detail.Star")));
        PagedList<FeedCommentDto> comments = processPagedFeedComments(FeedServiceApi.FEED.listComments(id, 0, Configuration.getInt("PageSize.Feed.Detail.Comment")));

        JSONObject feedDetailJson = new JSONObject();
        feedDetailJson.put("feed", processFeed(feed));
        feedDetailJson.put("staredUsers", stars);
        feedDetailJson.put("comments", comments);

        if (productId > 0) {
            ProductDto product = ProductServiceApi.PRODUCT.get(productId, ProductDto.Type.BASE);
            feedDetailJson.put("product", processProduct(product, ImageFile.Size.MIDDLE));
        }

        return MomiaHttpResponse.SUCCESS(feedDetailJson);
    }

    private PagedList<FeedStarDto> processPagedFeedStars(PagedList<FeedStarDto> stars) {
        for (FeedStarDto feedStar : stars.getList()) {
            feedStar.setAvatar(ImageFile.smallUrl(feedStar.getAvatar()));
        }

        return stars;
    }

    private PagedList<FeedCommentDto> processPagedFeedComments(PagedList<FeedCommentDto> comments) {
        for (FeedCommentDto feedComment : comments.getList()) {
            feedComment.setAvatar(ImageFile.smallUrl(feedComment.getAvatar()));
        }

        return comments;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse delete(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = UserServiceApi.USER.get(utoken);
        FeedServiceApi.FEED.delete(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.GET)
    public MomiaHttpResponse listComments(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<FeedCommentDto> comments = FeedServiceApi.FEED.listComments(id, start, Configuration.getInt("PageSize.Feed.Comment"));
        return MomiaHttpResponse.SUCCESS(processPagedFeedComments(comments));
    }

    @RequestMapping(value = "/comment/add", method = RequestMethod.POST)
    public MomiaHttpResponse addComment(@RequestParam String utoken, @RequestParam long id, @RequestParam String content) {
        if (StringUtils.isBlank(utoken) || id <= 0 || StringUtils.isBlank(content)) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = UserServiceApi.USER.get(utoken);
        FeedServiceApi.FEED.addComment(user.getId(), id, content);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteComment(@RequestParam String utoken, @RequestParam long id, @RequestParam(value = "cmid") long commentId) {
        if (StringUtils.isBlank(utoken) || id <= 0 || commentId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = UserServiceApi.USER.get(utoken);
        FeedServiceApi.FEED.deleteComment(user.getId(), id, commentId);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/star", method = RequestMethod.POST)
    public MomiaHttpResponse star(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = UserServiceApi.USER.get(utoken);
        FeedServiceApi.FEED.star(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unstar", method = RequestMethod.POST)
    public MomiaHttpResponse unstar(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        UserDto user = UserServiceApi.USER.get(utoken);
        FeedServiceApi.FEED.unstar(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }
}
