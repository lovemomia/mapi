package cn.momia.mapi.api.v1.im;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseSku;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.im.dto.Group;
import cn.momia.api.im.dto.Member;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.util.TimeUtil;
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
import java.util.Collections;
import java.util.Comparator;
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
    public MomiaHttpResponse getImUser(@RequestParam(value = "uid") long userId) {
        if (userId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(userId);
        JSONObject imUserJson = createImUser(user);

        List<String> latestImgs = courseServiceApi.getLatestImgs(userId);
        if (!latestImgs.isEmpty()) {
            imUserJson.put("imgs", completeMiddleImgs(latestImgs));
        }

        return MomiaHttpResponse.SUCCESS(imUserJson);
    }

    private JSONObject createImUser(User user) {
        JSONObject imUserJson = new JSONObject();
        imUserJson.put("id", user.getId());
        imUserJson.put("nickName", user.getNickName());
        imUserJson.put("avatar", completeSmallImg(user.getAvatar()));
        imUserJson.put("role", user.getRole());

        return imUserJson;
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
        List<Member> members = imServiceApi.listGroupMembers(user.getId(), groupId);

        Set<Long> userIds = new HashSet<Long>();
        for (Member member : members) {
            userIds.add(member.getUserId());
        }

        List<User> users = userServiceApi.list(userIds, User.Type.BASE);
        Map<Long, User> usersMap = new HashMap<Long, User>();
        for (User memberUser : users) {
            usersMap.put(memberUser.getId(), memberUser);
        }

        List<JSONObject> teachers = new ArrayList<JSONObject>();
        List<JSONObject> customers = new ArrayList<JSONObject>();
        for (Member member : members) {
            User memberUser = usersMap.get(member.getUserId());
            if (memberUser == null) continue;

            JSONObject imUserJson = createImUser(user);
            if (member.isTeacher()) teachers.add(imUserJson);
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

        List<Member> members = imServiceApi.queryMembersByUser(user.getId());
        Set<Long> groupIds = new HashSet<Long>();
        for (Member member : members) {
            groupIds.add(member.getGroupId());
        }
        List<Group> groups = imServiceApi.listGroups(groupIds);
        Map<Long, Group> groupsMap = new HashMap<Long, Group>();
        Set<Long> courseIds = new HashSet<Long>();
        for (Group group : groups) {
            groupsMap.put(group.getGroupId(), group);
            courseIds.add(group.getCourseId());
        }
        Map<Long, String> tipsOfCourses = courseServiceApi.queryTips(courseIds);

        List<JSONObject> userGroups = new ArrayList<JSONObject>();
        for (Member member : members) {
            Group group = groupsMap.get(member.getGroupId());
            if (group == null) continue;

            JSONObject userGroup = new JSONObject();
            userGroup.put("groupId", member.getGroupId());
            userGroup.put("groupName", group.getGroupName());
            userGroup.put("addTime", TimeUtil.STANDARD_DATE_FORMAT.format(member.getAddTime()));
            userGroup.put("tips", tipsOfCourses.get(String.valueOf(group.getCourseId())));  // FIXME

            userGroups.add(userGroup);
        }

        Collections.sort(userGroups, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return o1.getString("groupName").compareTo(o2.getString("groupName"));
            }
        });

        return MomiaHttpResponse.SUCCESS(userGroups);
    }
}
