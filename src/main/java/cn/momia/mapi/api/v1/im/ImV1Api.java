package cn.momia.mapi.api.v1.im;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseSku;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.im.dto.Group;
import cn.momia.api.im.dto.ImUser;
import cn.momia.api.im.dto.Member;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.util.TimeUtil;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
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
public class ImV1Api extends AbstractV1Api {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private ImServiceApi imServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public MomiaHttpResponse getImToken(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        String imToken = imServiceApi.getImToken(utoken);
        if (StringUtils.isBlank(imToken)) {
            User user = userServiceApi.get(utoken);
            imToken = imServiceApi.generateImToken(utoken, user.getNickName(), ImageFile.smallUrl(user.getAvatar()));
        }

        return MomiaHttpResponse.SUCCESS(imToken);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public MomiaHttpResponse getImUser(@RequestParam(value = "uid") long userId) {
        if (userId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        ImUser imUser = imServiceApi.getImUser(userId);
        imUser.setAvatar(ImageFile.smallUrl(imUser.getAvatar()));

        return MomiaHttpResponse.SUCCESS(imUser);
    }

    @RequestMapping(value = "/group", method = RequestMethod.GET)
    public MomiaHttpResponse getGroupInfo(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;
        Group group = imServiceApi.getGroup(id);
        CourseSku sku = courseServiceApi.getSku(group.getCourseId(), group.getCourseSkuId());
        JSONObject groupInfo = new JSONObject();
        groupInfo.put("groupId", group.getGroupId());
        groupInfo.put("groupName", group.getGroupName());
        groupInfo.put("tips", courseServiceApi.queryTips(Sets.newHashSet(group.getCourseId())).get(String.valueOf(group.getCourseId())));
        groupInfo.put("time", sku.getTime());
        groupInfo.put("address", sku.getPlace().getAddress());

        return MomiaHttpResponse.SUCCESS(groupInfo);
    }

    @RequestMapping(value = "/group/member", method = RequestMethod.GET)
    public MomiaHttpResponse listGroupMembers(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<ImUser> teachers = new ArrayList<ImUser>();
        List<ImUser> customers = new ArrayList<ImUser>();
        List<ImUser> members = imServiceApi.listGroupMembers(utoken, id);
        for (ImUser member : members) {
            member.setAvatar(ImageFile.smallUrl(member.getAvatar()));
            if (member.isTeacher()) teachers.add(member);
            else customers.add(member);
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("teachers", teachers);
        resultJson.put("customers", customers);

        return MomiaHttpResponse.SUCCESS(resultJson);
    }

    @RequestMapping(value = "/user/group", method = RequestMethod.GET)
    public MomiaHttpResponse listUserGroups(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        List<Member> members = imServiceApi.queryMembersByUser(utoken);
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
