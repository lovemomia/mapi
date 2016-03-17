package cn.momia.mapi.api.im;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.course.CourseSku;
import cn.momia.api.im.ImServiceApi;
import cn.momia.api.im.dto.Group;
import cn.momia.api.im.dto.GroupMember;
import cn.momia.api.im.dto.UserGroup;
import cn.momia.api.user.TeacherServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Teacher;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
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
    @Autowired private ImServiceApi imServiceApi;
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private UserServiceApi userServiceApi;
    @Autowired private TeacherServiceApi teacherServiceApi;

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public MomiaHttpResponse generateImToken(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(doGenerateImToken(userServiceApi.get(utoken)));
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
        if (userId <= 0) return MomiaHttpResponse.FAILED("无效的用户ID");

        User user = userServiceApi.get(userId);
        JSONObject imUserInfoJson = createImUserInfo(user);

        List<String> latestImgs = courseServiceApi.getLatestCommentImgs(userId);
        if (!latestImgs.isEmpty()) imUserInfoJson.put("imgs", completeMiddleImgs(latestImgs));

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

    @RequestMapping(value = "/user/group", method = RequestMethod.GET)
    public MomiaHttpResponse listUserGroups(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        User user = userServiceApi.get(utoken);
        List<UserGroup> userGroups = imServiceApi.listUserGroups(user.getId());

        Set<Long> courseIds = new HashSet<Long>();
        for (UserGroup group : userGroups) {
            courseIds.add(group.getCourseId());
        }
        Map<Long, String> tipsOfCourses = courseServiceApi.queryTips(courseIds);

        List<JSONObject> result = new ArrayList<JSONObject>();
        for (UserGroup userGroup : userGroups) {
            JSONObject userGroupJson = (JSONObject) JSON.toJSON(userGroup);
            userGroupJson.put("addTime", TimeUtil.STANDARD_DATE_FORMAT.format(userGroup.getAddTime()));
            userGroupJson.put("tips", tipsOfCourses.get(String.valueOf(userGroup.getCourseId())));

            result.add(userGroupJson);
        }

        return MomiaHttpResponse.SUCCESS(result);
    }

    @RequestMapping(value = "/group", method = RequestMethod.GET)
    public MomiaHttpResponse getGroupInfo(@RequestParam(value = "id") long groupId) {
        if (groupId <= 0) return MomiaHttpResponse.FAILED("无效的群组ID");

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
        if (groupId <= 0) return MomiaHttpResponse.FAILED("无效的群组ID");

        User user = userServiceApi.get(utoken);
        List<GroupMember> groupMembers = imServiceApi.listGroupMembers(user.getId(), groupId);

        Set<Long> teacherUserIds = new HashSet<Long>();
        Set<Long> customerUserIds = new HashSet<Long>();
        for (GroupMember groupMember : groupMembers) {
            if (groupMember.isTeacher()) teacherUserIds.add(groupMember.getUserId());
            else customerUserIds.add(groupMember.getUserId());
        }

        List<Teacher> teachers = teacherServiceApi.listByUserIds(teacherUserIds);
        Map<Long, Teacher> teachersMap = new HashMap<Long, Teacher>();
        for (Teacher teacher : teachers) {
            teachersMap.put(teacher.getUserId(), teacher);
        }

        List<User> customers = userServiceApi.list(customerUserIds, User.Type.BASE);
        Map<Long, User> customersMap = new HashMap<Long, User>();
        for (User customer : customers) {
            customersMap.put(customer.getId(), customer);
        }

        List<JSONObject> teacherJsons = new ArrayList<JSONObject>();
        List<JSONObject> customerJsons = new ArrayList<JSONObject>();
        for (GroupMember groupMember : groupMembers) {
            if (groupMember.isTeacher()) {
                Teacher teacher = teachersMap.get(groupMember.getUserId());
                if (teacher == null) continue;

                JSONObject imTeacherJson = createImTeacherInfo(teacher);
                teacherJsons.add(imTeacherJson);
            } else {
                User customer = customersMap.get(groupMember.getUserId());
                if (customer == null) continue;

                JSONObject imUserJson = createImUserInfo(customer);
                customerJsons.add(imUserJson);
            }
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("teachers", teacherJsons);
        responseJson.put("customers", customerJsons);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    private JSONObject createImTeacherInfo(Teacher teacher) {
        JSONObject imTeacherInfoJson = new JSONObject();
        imTeacherInfoJson.put("id", teacher.getUserId());
        imTeacherInfoJson.put("nickName", teacher.getName());
        imTeacherInfoJson.put("avatar", completeSmallImg(teacher.getAvatar()));
        imTeacherInfoJson.put("role", User.Role.TEACHER);

        return imTeacherInfoJson;
    }
}
