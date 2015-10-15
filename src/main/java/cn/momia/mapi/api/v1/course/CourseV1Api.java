package cn.momia.mapi.api.v1.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.UserDto;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/course")
public class CourseV1Api extends AbstractV1Api {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam long id) {
        CourseDto course = courseServiceApi.get(id);
        if (!StringUtils.isBlank(utoken)) {
            UserDto user = userServiceApi.get(utoken);
            course.setFavored(courseServiceApi.isFavored(user.getId(), id));
        }

        return MomiaHttpResponse.SUCCESS(processCourse(course));
    }

    @RequestMapping(value = "/sku/week", method = RequestMethod.GET)
    public MomiaHttpResponse listWeekSkus(@RequestParam long id) {
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listWeekSkus(id));
    }

    @RequestMapping(value = "/sku/month", method = RequestMethod.GET)
    public MomiaHttpResponse listWeekSkus(@RequestParam long id, @RequestParam int month) {
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listMonthSkus(id, month));
    }

    @RequestMapping(value = "/sku/more", method = RequestMethod.GET)
    public MomiaHttpResponse listMoreSkus(@RequestParam long id, @RequestParam String date, @RequestParam String excludes) {
        return MomiaHttpResponse.SUCCESS(courseServiceApi.listMoreSkus(id, date, excludes));
    }
//
//    @RequestMapping(value = "/booking", method = RequestMethod.POST)
//    public MomiaHttpResponse booking(@RequestParam String utoken, @RequestParam() String order) {
//        return MomiaHttpResponse.SUCCESS(courseServiceApi.booking());
//    }

    @RequestMapping(value = "/favor", method = RequestMethod.POST)
    public MomiaHttpResponse favor(@RequestParam String utoken, @RequestParam long id) {
        UserDto user = userServiceApi.get(utoken);
        if (!courseServiceApi.favor(user.getId(), id)) return MomiaHttpResponse.FAILED("添加收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }

    @RequestMapping(value = "/unfavor", method = RequestMethod.POST)
    public MomiaHttpResponse unfavor(@RequestParam String utoken, @RequestParam long id) {
        UserDto user = userServiceApi.get(utoken);
        if (!courseServiceApi.unfavor(user.getId(), id)) return MomiaHttpResponse.FAILED("取消收藏失败");
        return MomiaHttpResponse.SUCCESS;
    }
}
