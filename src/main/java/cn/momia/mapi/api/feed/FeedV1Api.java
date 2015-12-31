package cn.momia.mapi.api.feed;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.Feed;
import cn.momia.api.feed.dto.FeedComment;
import cn.momia.api.feed.dto.FeedTag;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.FeedRelatedApi;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/v1/feed")
public class FeedV1Api extends FeedRelatedApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedV1Api.class);

    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private FeedServiceApi feedServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    public MomiaHttpResponse follow(@RequestParam String utoken, @RequestParam(value = "fuid") long followedId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (followedId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User followedUser = userServiceApi.get(followedId);
        if (!followedUser.exists()) return MomiaHttpResponse.FAILED("关注的用户不存在");

        User user = userServiceApi.get(utoken);
        feedServiceApi.follow(user.getId(), followedId);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse list(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam int start) {
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        long userId = 0;
        if (!StringUtils.isBlank(utoken)) {
            User user = userServiceApi.get(utoken);
            userId = user.getId();
        }

        PagedList<Feed> pagedFeeds = feedServiceApi.list(userId, start, Configuration.getInt("PageSize.Feed"));
        return MomiaHttpResponse.SUCCESS(buildPagedUserFeeds(userId, pagedFeeds));
    }

    @RequestMapping(value = "/subject", method = RequestMethod.GET)
    public MomiaHttpResponse subject(@RequestParam(defaultValue = "") String utoken,
                                     @RequestParam(value = "suid") long subjectId,
                                     @RequestParam final int start) {
        if (subjectId <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        PagedList<Feed> pagedFeeds = feedServiceApi.queryBySubject(subjectId, start, Configuration.getInt("PageSize.Feed"));

        return MomiaHttpResponse.SUCCESS(buildPagedUserFeeds(userId, pagedFeeds));
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public MomiaHttpResponse course(@RequestParam(defaultValue = "") String utoken,
                                    @RequestParam(value = "coid") long courseId,
                                    @RequestParam final int start) {
        if (courseId <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject courseFeedsJson = new JSONObject();

        if (start == 0) {
            Course course = courseServiceApi.get(courseId, Course.ShowType.BASE);
            course.setCover(completeMiddleImg(course.getCover()));
            courseFeedsJson.put("course", course);
        }

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        PagedList<Feed> pagedFeeds = feedServiceApi.queryByCourse(courseId, start, Configuration.getInt("PageSize.Feed"));
        courseFeedsJson.put("feeds", buildPagedUserFeeds(userId, pagedFeeds));

        return MomiaHttpResponse.SUCCESS(courseFeedsJson);
    }

    @RequestMapping(value = "/course/list", method = RequestMethod.GET)
    public MomiaHttpResponse listCourses(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<? extends Course> pagedCourses;
        User user = userServiceApi.get(utoken);
        if (!feedServiceApi.isOfficialUser(user.getId())) {
            pagedCourses = courseServiceApi.listFinished(user.getId(), start, Configuration.getInt("PageSize.Course"));
        } else {
            pagedCourses = courseServiceApi.listFinished(0, start, Configuration.getInt("PageSize.Course"));
        }
        
        for (Course course : pagedCourses.getList()) {
            course.setCover(completeMiddleImg(course.getCover()));
        }

        return MomiaHttpResponse.SUCCESS(pagedCourses);
    }

    @RequestMapping(value = "/tag", method = RequestMethod.GET)
    public MomiaHttpResponse listTags() {
        List<FeedTag> recommendedTags = feedServiceApi.listRecommendedTags(Configuration.getInt("PageSize.FeedTag"));
        List<FeedTag> hotTags = feedServiceApi.listHotTags(Configuration.getInt("PageSize.FeedTag"));
        JSONObject tagsJson = new JSONObject();
        tagsJson.put("recommendedTags", recommendedTags);
        tagsJson.put("hotTags", hotTags);

        return MomiaHttpResponse.SUCCESS(tagsJson);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public MomiaHttpResponse add(@RequestParam String utoken, @RequestParam String feed) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(feed)) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        JSONObject feedJson = JSON.parseObject(feed);
        feedJson.put("userId", user.getId());

        Long courseId = feedJson.getLong("courseId");
        if (courseId != null && courseId > 0) {
            Course course = courseServiceApi.get(courseId, Course.ShowType.BASE);
            feedJson.put("subjectId", course.getSubjectId());
            if (!feedServiceApi.isOfficialUser(user.getId()) && !courseServiceApi.joined(user.getId(), feedJson.getLong("courseId"))) return MomiaHttpResponse.FAILED("发表Feed失败，所选课程不存在或您还没参加该课程");
        }

        feedServiceApi.add(feedJson);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public MomiaHttpResponse detail(@RequestParam(defaultValue = "") String utoken, @RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        Feed feed = feedServiceApi.get(id);

        PagedList<Long> pagedStaredUserIds = feedServiceApi.listStaredUserIds(id, 0, Configuration.getInt("PageSize.FeedDetailStar"));
        PagedList<FeedComment> pagedFeedComments = feedServiceApi.listComments(id, 0, Configuration.getInt("PageSize.FeedDetailComment"));

        JSONObject feedDetailJson = new JSONObject();
        feedDetailJson.put("feed", buildUserFeed(userId, feed));
        feedDetailJson.put("staredUsers", buildStaredUsers(pagedStaredUserIds));
        feedDetailJson.put("comments", buildUserFeedComments(pagedFeedComments));

        if (feed.getCourseId() > 0) {
            try {
                Course course = courseServiceApi.get(feed.getCourseId(), Course.ShowType.BASE);
                course.setCover(completeMiddleImg(course.getCover()));

                feedDetailJson.put("course", course);
            } catch (Exception e) {
                LOGGER.error("fail to get course info: {}", feed.getCourseId());
            }
        }

        return MomiaHttpResponse.SUCCESS(feedDetailJson);
    }

    private PagedList<User> buildStaredUsers(PagedList<Long> pagedStaredUserIds) {
        List<User> staredUsers = userServiceApi.list(pagedStaredUserIds.getList(), User.Type.MINI);
        completeUsersImgs(staredUsers);

        PagedList<User> pagedStaredUsers = new PagedList<User>();
        pagedStaredUsers.setTotalCount(pagedStaredUserIds.getTotalCount());
        pagedStaredUsers.setNextIndex(pagedStaredUserIds.getNextIndex());
        pagedStaredUsers.setList(staredUsers);

        return pagedStaredUsers;
    }

    private PagedList<JSONObject> buildUserFeedComments(PagedList<FeedComment> pagedFeedComments) {
        Set<Long> userIds = new HashSet<Long>();
        for (FeedComment comment : pagedFeedComments.getList()) {
            userIds.add(comment.getUserId());
        }
        List<User> users = userServiceApi.list(userIds, User.Type.MINI);
        Map<Long, User> usersMap = new HashMap<Long, User>();
        for (User user : users) {
            usersMap.put(user.getId(), user);
        }

        List<JSONObject> userFeedComments = new ArrayList<JSONObject>();
        for (FeedComment comment : pagedFeedComments.getList()) {
            User user = usersMap.get(comment.getUserId());
            if (user == null) continue;

            userFeedComments.add(buildUserFeedComment(comment, user));
        }

        PagedList<JSONObject> pagedUserFeedComments = new PagedList<JSONObject>();
        pagedUserFeedComments.setTotalCount(pagedFeedComments.getTotalCount());
        pagedUserFeedComments.setNextIndex(pagedFeedComments.getNextIndex());
        pagedUserFeedComments.setList(userFeedComments);

        return pagedUserFeedComments;
    }

    private JSONObject buildUserFeedComment(FeedComment comment, User user) {
        JSONObject userFeedComment = new JSONObject();
        userFeedComment.put("id", comment.getId());
        userFeedComment.put("content", comment.getContent());
        userFeedComment.put("addTime", TimeUtil.formatAddTime(comment.getAddTime()));
        userFeedComment.put("nickName", user.getNickName());
        userFeedComment.put("avatar", completeSmallImg(user.getAvatar()));

        return userFeedComment;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public MomiaHttpResponse delete(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        if (!feedServiceApi.delete(user.getId(), id)) return MomiaHttpResponse.FAILED("删除Feed失败");

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.GET)
    public MomiaHttpResponse listComments(@RequestParam long id, @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<FeedComment> pagedComments = feedServiceApi.listComments(id, start, Configuration.getInt("PageSize.FeedComment"));
        return MomiaHttpResponse.SUCCESS(buildUserFeedComments(pagedComments));
    }

    @RequestMapping(value = "/comment/add", method = RequestMethod.POST)
    public MomiaHttpResponse addComment(@RequestParam String utoken, @RequestParam long id, @RequestParam String content) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0 || StringUtils.isBlank(content)) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        feedServiceApi.addComment(user.getId(), id, content);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/comment/delete", method = RequestMethod.POST)
    public MomiaHttpResponse deleteComment(@RequestParam String utoken, @RequestParam long id, @RequestParam(value = "cmid") long commentId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0 || commentId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        feedServiceApi.deleteComment(user.getId(), id, commentId);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/star", method = RequestMethod.POST)
    public MomiaHttpResponse star(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        feedServiceApi.star(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unstar", method = RequestMethod.POST)
    public MomiaHttpResponse unstar(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        feedServiceApi.unstar(user.getId(), id);

        return MomiaHttpResponse.SUCCESS;
    }
}
