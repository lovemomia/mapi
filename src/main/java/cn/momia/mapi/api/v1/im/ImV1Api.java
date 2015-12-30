package cn.momia.mapi.api.v1.im;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseSku;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.im.dto.Group;
import cn.momia.api.im.dto.GroupMember;
import cn.momia.api.im.dto.UserGroup;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
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
@RequestMapping("/v1/im")
public class ImV1Api extends AbstractApi {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private ImServiceApi imServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public MomiaHttpResponse generateImToken(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        User user = userServiceApi.get(utoken);
        return MomiaHttpResponse.SUCCESS(doGenerateImToken(user));
    }

    private String doGenerateImToken(User user) {
        String imToken = imServiceApi.generateImToken(user.getId(), user.getNickName(), completeSmallImg(user.getAvatar()));
        if (!StringUtils.isBlank(imToken)) userServiceApi.updateImToken(user.getToken(), imToken);

        return imToken;
    }

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public MomiaHttpResponse getImToken(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        User user = userServiceApi.get(utoken);
        String imToken = user.getImToken();
        if (StringUtils.isBlank(imToken)) imToken = doGenerateImToken(user);

        return MomiaHttpResponse.SUCCESS(imToken);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public MomiaHttpResponse getImUserInfo(@RequestParam(value = "uid") long userId) {
        if (userId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(userId);
        JSONObject imUserInfoJson = createImUserInfo(user);

        List<String> latestImgs = courseServiceApi.getLatestImgs(userId);
        if (!latestImgs.isEmpty()) {
            imUserInfoJson.put("imgs", completeMiddleImgs(latestImgs));
        }

        return MomiaHttpResponse.SUCCESS(imUserInfoJson);
    }

    private JSONObject createImUserInfo(User user) {
        JSONObject imUserInfoJson = new JSONObject();
        imUserInfoJson.put("id", user.getId());
        imUserInfoJson.put("nickName", user.getNickName());
        imUserInfoJson.put("avatar", completeSmallImg(user.getAvatar()));
        imUserInfoJson.put("role", user.getRole());

        return imUserInfoJson;
    }

    @RequestMapping(value = "/group", method = RequestMethod.GET)
    public MomiaHttpResponse getGroupInfo(@RequestParam(value = "id") long groupId) {
        if (groupId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Group group = imServiceApi.getGroup(groupId);
        CourseSku sku = courseServiceApi.getSku(group.getCourseId(), group.getCourseSkuId());

        JSONObject groupInfo = new JSONObject();
        groupInfo.put("groupId", group.getGroupId());
        groupInfo.put("groupName", group.getGroupName());
        groupInfo.put("tips", courseServiceApi.queryTips(Sets.newHashSet(group.getCourseId())).get(String.valueOf(group.getCourseId())));
        groupInfo.put("time", sku.getTime());
        groupInfo.put("route", sku.getRoute());
        groupInfo.put("address", sku.getPlace().getAddress());

        return MomiaHttpResponse.SUCCESS(groupInfo);
    }

    @RequestMapping(value = "/group/member", method = RequestMethod.GET)
    public MomiaHttpResponse listGroupMembers(@RequestParam String utoken, @RequestParam(value = "id") long groupId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (groupId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        List<GroupMember> groupMembers = imServiceApi.listGroupMembers(user.getId(), groupId);

        Set<Long> userIds = new HashSet<Long>();
        for (GroupMember groupMember : groupMembers) {
            userIds.add(groupMember.getUserId());
        }

        List<User> users = userServiceApi.list(userIds, User.Type.BASE);
        Map<Long, User> usersMap = new HashMap<Long, User>();
        for (User memberUser : users) {
            usersMap.put(memberUser.getId(), memberUser);
        }

        List<JSONObject> teachers = new ArrayList<JSONObject>();
        List<JSONObject> customers = new ArrayList<JSONObject>();
        for (GroupMember groupMember : groupMembers) {
            User memberUser = usersMap.get(groupMember.getUserId());
            if (memberUser == null) continue;

            JSONObject imUserJson = createImUserInfo(memberUser);
            if (groupMember.isTeacher()) teachers.add(imUserJson);
            else customers.add(imUserJson);
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("teachers", teachers);
        responseJson.put("customers", customers);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/user/group", method = RequestMethod.GET)
    public MomiaHttpResponse listUserGroups(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        User user = userServiceApi.get(utoken);
        List<UserGroup> userGroups = imServiceApi.listUserGroups(user.getId());

        Set<Long> courseIds = new HashSet<Long>();
        for (UserGroup userGroup : userGroups) {
            courseIds.add(userGroup.getCourseId());
        }
        Map<Long, String> tipsOfCourses = courseServiceApi.queryTips(courseIds);

        for (UserGroup userGroup : userGroups) {
            userGroup.setTips(tipsOfCourses.get(String.valueOf(userGroup.getCourseId())));
        }

        return MomiaHttpResponse.SUCCESS(userGroups);
    }
}
