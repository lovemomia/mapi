package cn.momia.mapi.api.v1.course;

import cn.momia.api.base.MetaUtil;
import cn.momia.api.base.dto.AgeRangeDto;
import cn.momia.api.base.dto.SortTypeDto;
import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.api.course.dto.SubjectSkuDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.ContactDto;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.v1.AbstractV1Api;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
        SubjectDto subject = processSubject(subjectServiceApi.get(id));
        PagedList<CourseDto> courses = processPagedCourses(courseServiceApi.listBySubject(id, 0, 2));

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("courses", courses);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public MomiaHttpResponse listCourses(@RequestParam long id,
                                         @RequestParam(required = false, defaultValue = "0") int age,
                                         @RequestParam(required = false, defaultValue = "0") int sort,
                                         @RequestParam int start) {
        AgeRangeDto ageRange = MetaUtil.getAgeRange(age);
        SortTypeDto sortType = MetaUtil.getSortType(sort);

        PagedList<CourseDto> courses = courseServiceApi.query(id, ageRange.getMin(), ageRange.getMax(), sortType.getId(), start, Configuration.getInt("PageSize.Course"));

        JSONObject responseJson = new JSONObject();
        responseJson.put("ages", MetaUtil.listAgeRanges());
        responseJson.put("sorts", MetaUtil.listSortTypes());
        responseJson.put("currentAge", ageRange.getId());
        responseJson.put("currentSort", sortType.getId());
        responseJson.put("courses", processPagedCourses(courses));

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public MomiaHttpResponse order(@RequestParam String utoken, @RequestParam long id) {
        List<SubjectSkuDto> skus = subjectServiceApi.listSkus(id);
        ContactDto contact = userServiceApi.getContact(utoken);

        JSONObject responseJson = new JSONObject();
        responseJson.put("skus", skus);
        responseJson.put("contact", contact);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public MomiaHttpResponse order(@RequestParam String utoken, @RequestParam String order) {
        UserDto user = userServiceApi.get(utoken);
        JSONObject orderJson = JSON.parseObject(order);
        orderJson.put("userId", user.getId());

        return MomiaHttpResponse.SUCCESS(subjectServiceApi.placeOrder(orderJson));
    }
}
