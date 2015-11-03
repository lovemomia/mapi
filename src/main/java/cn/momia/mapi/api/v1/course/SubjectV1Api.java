package cn.momia.mapi.api.v1.course;

import cn.momia.api.base.MetaUtil;
import cn.momia.api.base.dto.AgeRangeDto;
import cn.momia.api.base.dto.SortTypeDto;
import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.CourseCommentDto;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.api.course.dto.SubjectSkuDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.ContactDto;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/subject")
public class SubjectV1Api extends AbstractV1Api {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        SubjectDto subject = subjectServiceApi.get(id);
        subject.setCover(ImageFile.largeUrl(subject.getCover()));
        processLargeImgs(subject.getImgs());

        PagedList<CourseDto> courses = courseServiceApi.query(id, 0, 2);
        processCourses(courses.getList());

        PagedList<CourseCommentDto> comments = courseServiceApi.queryCommentsBySubject(id, 0, 1);
        processCourseComments(comments.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("courses", courses);
        if (!comments.getList().isEmpty()) responseJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    private void processCourses(List<CourseDto> courses) {
        for (CourseDto course : courses) {
            course.setCover(ImageFile.middleUrl(course.getCover()));
        }
    }

    private void processCourseComments(List<CourseCommentDto> comments) {
        for (CourseCommentDto comment : comments) {
            comment.setAvatar(ImageFile.smallUrl(comment.getAvatar()));
            List<String> imgs = comment.getImgs();
            comment.setImgs(processSmallImgs(imgs));
            comment.setLargeImgs(processLargeImgs(imgs));
        }
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public MomiaHttpResponse listCourses(@RequestParam long id,
                                         @RequestParam(required = false, defaultValue = "0") int age,
                                         @RequestParam(required = false, defaultValue = "0") int sort,
                                         @RequestParam int start) {
        if (id <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        AgeRangeDto ageRange = MetaUtil.getAgeRange(age);
        SortTypeDto sortType = MetaUtil.getSortType(sort);

        PagedList<CourseDto> courses = courseServiceApi.query(id, ageRange.getMin(), ageRange.getMax(), sortType.getId(), start, Configuration.getInt("PageSize.Course"));
        processCourses(courses.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("ages", MetaUtil.listAgeRanges());
        responseJson.put("sorts", MetaUtil.listSortTypes());
        responseJson.put("currentAge", ageRange.getId());
        responseJson.put("currentSort", sortType.getId());
        responseJson.put("courses", courses);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/sku", method = RequestMethod.GET)
    public MomiaHttpResponse sku(@RequestParam String utoken, @RequestParam long id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<SubjectSkuDto> skus = subjectServiceApi.querySkus(id);
        ContactDto contact = userServiceApi.getContact(utoken);

        JSONObject responseJson = new JSONObject();
        responseJson.put("skus", skus);
        responseJson.put("contact", contact);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public MomiaHttpResponse order(@RequestParam String utoken, @RequestParam String order) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(order)) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject orderJson = JSON.parseObject(order);

        UserDto user = userServiceApi.get(utoken);
        orderJson.put("userId", user.getId());

        JSONObject contactJson = orderJson.getJSONObject("contact");
        orderJson.put("contact", contactJson.getString("name"));
        orderJson.put("mobile", contactJson.getString("mobile"));

        JSONArray packagesJson = new JSONArray();
        JSONArray skusJson = orderJson.getJSONArray("skus");
        for (int i = 0; i < skusJson.size(); i++) {
            JSONObject skuJson = skusJson.getJSONObject(i);
            orderJson.put("subjectId", skuJson.getLong("subjectId"));
            int count = skuJson.getInteger("count");
            for (int j = 0; j < count; j++) {
                JSONObject packageJson = new JSONObject();
                packageJson.put("skuId", skuJson.getLong("id"));
                packagesJson.add(packageJson);
            }
        }
        orderJson.put("packages", packagesJson);

        return MomiaHttpResponse.SUCCESS(subjectServiceApi.placeOrder(orderJson));
    }
}
