package cn.momia.mapi.api.v1;

import cn.momia.mapi.common.config.Configuration;
import cn.momia.mapi.common.img.ImageFile;
import cn.momia.mapi.web.response.ResponseMessage;
import cn.momia.service.feed.api.FeedServiceApi;
import cn.momia.service.feed.api.comment.FeedComment;
import cn.momia.service.feed.api.comment.PagedFeedComments;
import cn.momia.service.feed.api.feed.Feed;
import cn.momia.service.feed.api.feed.PagedFeeds;
import cn.momia.service.feed.api.star.FeedStar;
import cn.momia.service.product.api.ProductServiceApi;
import cn.momia.service.product.api.product.Product;
import cn.momia.service.user.api.UserServiceApi;
import cn.momia.service.user.api.user.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/feed")
public class FeedV1Api extends AbstractV1Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedV1Api.class);

    @Autowired private FeedServiceApi feedServiceApi;
    @Autowired private ProductServiceApi productServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseMessage list(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken) || start < 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        PagedFeeds feeds = feedServiceApi.FEED.list(user.getId(), start, Configuration.getInt("PageSize.Feed.List"));

        return ResponseMessage.SUCCESS(processPagedFeeds(feeds));
    }

    private PagedFeeds processPagedFeeds(PagedFeeds feeds) {
        for (Feed feed : feeds.getList()) {
            processFeed(feed);
        }

        return feeds;
    }

    private Feed processFeed(Feed feed) {
        feed.setAvatar(ImageFile.url(feed.getAvatar()));
        if (feed.getImgs() != null) {
            for (int i = 0; i < feed.getImgs().size(); i++) {
                feed.getImgs().set(i, ImageFile.middleUrl(feed.getImgs().get(i)));
            }
        }

        return feed;
    }

    @RequestMapping(value = "/topic", method = RequestMethod.GET)
    public ResponseMessage topic(@RequestParam(defaultValue = "") String utoken,
                                 @RequestParam(value = "pid") long productId,
                                 @RequestParam(value = "tid") long topicId,
                                 @RequestParam final int start) {
        if (topicId <= 0 || productId <= 0 || start < 0) return ResponseMessage.BAD_REQUEST;

        Product product = null;
        if (start == 0) product = processProduct(productServiceApi.PRODUCT.get(productId, false));

        long userId = 0;
        try {
            if (!StringUtils.isBlank(utoken)) userId = userServiceApi.USER.get(utoken).getId();
        } catch (Exception e) {
            LOGGER.error("exception!!", e);
        }

        PagedFeeds feeds = processPagedFeeds(feedServiceApi.FEED.listByTopic(userId, topicId, start, Configuration.getInt("PageSize.Feed.List")));

        JSONObject feedTopicJson = new JSONObject();
        if (start == 0) feedTopicJson.put("product", product);
        feedTopicJson.put("feeds", feeds);

        return ResponseMessage.SUCCESS(feedTopicJson);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseMessage add(@RequestParam String utoken, @RequestParam String feed) {
        if (StringUtils.isBlank(utoken) || StringUtils.isBlank(feed)) return ResponseMessage.BAD_REQUEST;

        JSONObject feedJson = JSON.parseObject(feed);
        JSONObject baseFeedJson = feedJson.getJSONObject("baseFeed");
        if (baseFeedJson == null) return ResponseMessage.BAD_REQUEST;
        baseFeedJson.put("userId", userServiceApi.USER.get(utoken));
        feedServiceApi.add(feedJson);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public ResponseMessage detail(@RequestParam(defaultValue = "") String utoken, @RequestParam long id, @RequestParam(value = "pid") long productId) {
        if (id <= 0 || productId <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        Feed feed = feedServiceApi.FEED.get(user.getId(), id);
        Product product = productServiceApi.PRODUCT.get(productId, false);
        List<FeedStar> stars = processPagedFeedStars(feedServiceApi.FEED.listStars(id, 0, Configuration.getInt("PageSize.Feed.Detail.Star"))).getList();
        List<FeedComment> comments = processPagedFeedComments(feedServiceApi.FEED.listComments(id, 0, Configuration.getInt("PageSize.Feed.Detail.Comment"))).getList();

        JSONObject feedDetailJson = new JSONObject();
        feedDetailJson.put("feed", processFeed(feed));
        feedDetailJson.put("product", processProduct(product));
        feedDetailJson.put("staredUsers", stars);
        feedDetailJson.put("comments", comments);

        return ResponseMessage.SUCCESS(feedDetailJson);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseMessage delete(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        feedServiceApi.FEED.delete(user.getId(), id);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.GET)
    public ResponseMessage listComments(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return ResponseMessage.BAD_REQUEST;

        PagedFeedComments comments = feedServiceApi.FEED.listComments(id, start, Configuration.getInt("PageSize.Feed.Comment"));
        return ResponseMessage.SUCCESS(processPagedFeedComments(comments));
    }

    @RequestMapping(value = "/comment/add", method = RequestMethod.POST)
    public ResponseMessage addComment(@RequestParam String utoken, @RequestParam long id, @RequestParam String content) {
        if (StringUtils.isBlank(utoken) || id <= 0 || StringUtils.isBlank(content)) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        feedServiceApi.addComment(user.getId(), id, content);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/comment/delete", method = RequestMethod.POST)
    public ResponseMessage deleteComment(@RequestParam String utoken, @RequestParam long id, @RequestParam(value = "cmid") long commentId) {
        if (StringUtils.isBlank(utoken) || id <= 0 || commentId <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        feedServiceApi.deleteComment(user.getId(), id, commentId);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/star", method = RequestMethod.POST)
    public ResponseMessage star(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        feedServiceApi.FEED.star(user.getId(), id);

        return ResponseMessage.SUCCESS;
    }

    @RequestMapping(value = "/unstar", method = RequestMethod.POST)
    public ResponseMessage unstar(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken) || id <= 0) return ResponseMessage.BAD_REQUEST;

        User user = userServiceApi.USER.get(utoken);
        feedServiceApi.FEED.unstar(user.getId(), id);

        return ResponseMessage.SUCCESS;
    }
}

