package cn.momia.mapi.api.discuss;

import cn.momia.api.discuss.DiscussServiceApi;
import cn.momia.api.discuss.dto.DiscussReply;
import cn.momia.api.discuss.dto.DiscussTopic;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Child;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/v1/discuss")
public class DiscussV1Api extends AbstractApi {
    @Autowired private DiscussServiceApi discussServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/topic", method = RequestMethod.GET)
    public MomiaHttpResponse getTopic(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam int id, @RequestParam int start) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的TopicID");
        if (start < 0) return MomiaHttpResponse.FAILED("无效的分页参数，start必须为非负整数");

        JSONObject responseJson = new JSONObject();

        if (start == 0) responseJson.put("topic", completeDiscussTopic(discussServiceApi.getTopic(id)));

        PagedList<DiscussReply> pagedReplies = discussServiceApi.listReplies(id, start, Configuration.getInt("PageSize.DiscussReply"));

        Set<Long> replyIds = new HashSet<Long>();
        Set<Long> userIds = new HashSet<Long>();
        for (DiscussReply reply : pagedReplies.getList()) {
            replyIds.add(reply.getId());
            userIds.add(reply.getUserId());
        }

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        List<Long> staredReplyIds = userId > 0 ? discussServiceApi.filterNotStaredReplyIds(userId, replyIds) : new ArrayList<Long>();

        List<User> users = userServiceApi.list(userIds, User.Type.FULL);

        responseJson.put("replies", buildPagedUserReplies(pagedReplies, staredReplyIds, users));

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    private DiscussTopic completeDiscussTopic(DiscussTopic topic) {
        topic.setCover(completeLargeImg(topic.getCover()));
        return topic;
    }

    private PagedList<JSONObject> buildPagedUserReplies(PagedList<DiscussReply> pagedReplies, List<Long> staredReplyIds, List<User> users) {
        PagedList<JSONObject> pagedUserReplies = new PagedList<JSONObject>();
        pagedUserReplies.setTotalCount(pagedReplies.getTotalCount());
        pagedUserReplies.setNextIndex(pagedReplies.getNextIndex());

        Map<Long, User> usersMap = new HashMap<Long, User>();
        for (User user : users) {
            usersMap.put(user.getId(), user);
        }

        List<JSONObject> userReplies = new ArrayList<JSONObject>();
        for (DiscussReply reply : pagedReplies.getList()) {
            User user = usersMap.get(reply.getUserId());
            if (user == null) continue;

            JSONObject userReply = new JSONObject();
            userReply.put("id", reply.getId());
            userReply.put("avatar", completeSmallImg(user.getAvatar()));
            userReply.put("nickName", user.getNickName());
            List<JSONObject> childrenDetail = formatChildrenDetail(user.getChildren());
            List<String> children = formatChildren(childrenDetail);
            userReply.put("childrenDetail", childrenDetail);
            userReply.put("children", children);
            userReply.put("content", reply.getContent());
            userReply.put("addTime", TimeUtil.formatAddTime(reply.getAddTime()));
            userReply.put("staredCount", reply.getStaredCount());
            userReply.put("stared", staredReplyIds.contains(reply.getId()));

            userReplies.add(userReply);
        }
        pagedUserReplies.setList(userReplies);

        return pagedUserReplies;
    }

    private List<JSONObject> formatChildrenDetail(List<Child> children) {
        List<JSONObject> childrenDetail = new ArrayList<JSONObject>();
        for (int i = 0; i < Math.min(2, children.size()); i++) {
            Child child = children.get(i);
            JSONObject childJson = new JSONObject();
            childJson.put("sex", child.getSex());
            childJson.put("name", child.getName());
            childJson.put("age", TimeUtil.formatAge(child.getBirthday()));

            childrenDetail.add(childJson);
        }

        return childrenDetail;
    }

    private List<String> formatChildren(List<JSONObject> childrenDetail) {
        List<String> formatedChildren = new ArrayList<String>();
        for (JSONObject child : childrenDetail) {
            formatedChildren.add(child.getString("sex") + "孩" + child.getString("age"));
        }

        return formatedChildren;
    }

    @RequestMapping(value = "/reply", method = RequestMethod.POST)
    public MomiaHttpResponse reply(@RequestParam String utoken, @RequestParam(value = "topicid") int topicId, @RequestParam String content) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (topicId <= 0) return MomiaHttpResponse.FAILED("无效的TopicID");
        if (StringUtils.isBlank(content)) return MomiaHttpResponse.FAILED("内容不能为空");

        User user = userServiceApi.get(utoken);
        return MomiaHttpResponse.SUCCESS(discussServiceApi.reply(user.getId(), topicId, content));
    }

    @RequestMapping(value = "/reply/star", method = RequestMethod.POST)
    public MomiaHttpResponse star(@RequestParam String utoken, @RequestParam(value = "replyid") long replyId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (replyId <= 0) return MomiaHttpResponse.FAILED("无效的ReplyID");

        User user = userServiceApi.get(utoken);
        return MomiaHttpResponse.SUCCESS(discussServiceApi.star(user.getId(), replyId));
    }

    @RequestMapping(value = "/reply/unstar", method = RequestMethod.POST)
    public MomiaHttpResponse unstar(@RequestParam String utoken, @RequestParam(value = "replyid") long replyId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (replyId <= 0) return MomiaHttpResponse.FAILED("无效的ReplyID");

        User user = userServiceApi.get(utoken);
        return MomiaHttpResponse.SUCCESS(discussServiceApi.unstar(user.getId(), replyId));
    }
}
