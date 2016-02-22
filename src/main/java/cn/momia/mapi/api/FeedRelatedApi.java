package cn.momia.mapi.api;

import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.Feed;
import cn.momia.api.user.ChildServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.exception.MomiaErrorException;
import cn.momia.common.core.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeedRelatedApi extends AbstractApi {
    @Autowired private FeedServiceApi feedServiceApi;
    @Autowired private UserServiceApi userServiceApi;
    @Autowired private ChildServiceApi childServiceApi;

    protected PagedList<JSONObject> buildPagedUserFeeds(long userId, PagedList<Feed> pagedFeeds) {
        PagedList<JSONObject> pagedUserFeeds = new PagedList<JSONObject>();
        pagedUserFeeds.setTotalCount(pagedFeeds.getTotalCount());
        pagedUserFeeds.setNextIndex(pagedFeeds.getNextIndex());
        pagedUserFeeds.setList(buildUserFeeds(userId, pagedFeeds.getList()));

        return pagedUserFeeds;
    }

    private List<JSONObject> buildUserFeeds(long userId, List<Feed> feeds) {
        Set<Long> staredFeedIds = new HashSet<Long>();
        if (userId > 0) {
            Set<Long> feedIds = new HashSet<Long>();
            for (Feed feed : feeds) {
                feedIds.add(feed.getId());
            }
            staredFeedIds.addAll(feedServiceApi.filterNotStaredFeedIds(userId, feedIds));
        }

        Set<Long> userIds = new HashSet<Long>();
        for (Feed feed : feeds) {
            userIds.add(feed.getUserId());
        }

        List<User> users = userServiceApi.list(userIds, User.Type.FULL);
        Map<Long, User> usersMap = new HashMap<Long, User>();
        for (User user : users) {
            usersMap.put(user.getId(), user);
        }

        List<JSONObject> userFeeds = new ArrayList<JSONObject>();
        for (Feed feed : feeds) {
            User user = usersMap.get(feed.getUserId());
            if (user == null) continue;

            userFeeds.add(buildUserFeed(feed, user, staredFeedIds.contains(feed.getId())));
        }

        return userFeeds;
    }

    private JSONObject buildUserFeed(Feed feed, User user, boolean stared) {
        JSONObject userFeed = (JSONObject) JSON.toJSON(feed);
        userFeed.put("imgs", completeMiddleImgs(feed.getImgs()));
        userFeed.put("largeImgs", completeLargeImgs(feed.getImgs()));
        userFeed.put("addTime", TimeUtil.formatAddTime(feed.getAddTime()));
        userFeed.put("poi", feed.getLng() + ":" + feed.getLat());

        userFeed.put("userId", user.getId());
        userFeed.put("avatar", completeSmallImg(user.getAvatar()));
        userFeed.put("nickName", user.getNickName());

        List<JSONObject> childrenDetail = childServiceApi.formatChildrenDetail(user.getChildren());
        List<String> children = childServiceApi.formatChildren(childrenDetail);
        userFeed.put("childrenDetail", childrenDetail);
        userFeed.put("children", children);

        userFeed.put("stared", stared);

        return userFeed;
    }

    protected JSONObject buildUserFeed(long userId, Feed feed) {
        List<Feed> feeds = Lists.newArrayList(feed);
        List<JSONObject> userFeeds = buildUserFeeds(userId, feeds);

        if (userFeeds.isEmpty()) throw new MomiaErrorException("无效的Feed");

        return userFeeds.get(0);
    }
}
