package cn.momia.mapi.api.v1.feed;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.FeedCommentDto;
import cn.momia.api.feed.dto.FeedDto;
import cn.momia.api.feed.dto.FeedStarDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSONObject;
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
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private FeedServiceApi feedServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    public MomiaHttpResponse follow(@RequestParam String utoken, @RequestParam(value = "fuid") long followedId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (followedId <= 0) return MomiaHttpResponse.BAD_REQUEST;
        if (!userServiceApi.exists(followedId)) return MomiaHttpResponse.FAILED("关注的用户不存在");

        UserDto user = userServiceApi.get(utoken);
        feedServiceApi.follow(user.getId(), followedId);

        return MomiaHttpResponse.SUCCESS;
    }

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
            processFeed(feed);
        }
    }

    private void processFeed(FeedDto feed) {
        List<String> imgs = feed.getImgs();
        feed.setImgs(completeMiddleImgs(imgs));
        feed.setLargeImgs(completeLargeImgs(imgs));
        feed.setAvatar(ImageFile.smallUrl(feed.getAvatar()));
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public MomiaHttpResponse topic(@RequestParam(defaultValue = "") String utoken,
                                   @RequestParam(value = "coid") long courseId,
                                   @RequestParam final int start) {
        if (courseId <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject courseFeedsJson = new JSONObject();

        if (start == 0) {
            CourseDto course = courseServiceApi.get(courseId, CourseDto.Type.BASE);
            course.setCover(ImageFile.largeUrl(course.getCover()));
            courseFeedsJson.put("course", course);
        }

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        PagedList<FeedDto> pagedFeeds = feedServiceApi.queryByCourse(userId, courseId, start, Configuration.getInt("PageSize.Feed"));
        processFeeds(pagedFeeds.getList());
        courseFeedsJson.put("feeds", pagedFeeds);

        return MomiaHttpResponse.SUCCESS(courseFeedsJson);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public MomiaHttpResponse detail(@RequestParam(defaultValue = "") String utoken, @RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        FeedDto feed = feedServiceApi.get(userId, id);
        processFeed(feed);

        PagedList<FeedStarDto> stars = feedServiceApi.listStars(id, 0, Configuration.getInt("PageSize.FeedDetailStar"));
        processStars(stars.getList());
        PagedList<FeedCommentDto> comments = feedServiceApi.listComments(id, 0, Configuration.getInt("PageSize.FeedDetailComment"));
        processComments(comments.getList());

        CourseDto course = courseServiceApi.get(feed.getCourseId(), CourseDto.Type.BASE);
        course.setCover(ImageFile.largeUrl(course.getCover()));

        JSONObject feedDetailJson = new JSONObject();
        feedDetailJson.put("feed", feed);
        feedDetailJson.put("staredUsers", stars);
        feedDetailJson.put("comments", comments);
        feedDetailJson.put("course", course);

        return MomiaHttpResponse.SUCCESS(feedDetailJson);
    }

    private void processStars(List<FeedStarDto> stars) {
        for (FeedStarDto star : stars) {
            star.setAvatar(ImageFile.smallUrl(star.getAvatar()));
        }
    }

    private void processComments(List<FeedCommentDto> comments) {
        for (FeedCommentDto comment : comments) {
            comment.setAvatar(ImageFile.smallUrl(comment.getAvatar()));
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
