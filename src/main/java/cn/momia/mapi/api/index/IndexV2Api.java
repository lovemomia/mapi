package cn.momia.mapi.api.index;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.course.Course;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v2/index")
public class IndexV2Api extends AbstractIndexApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexV2Api.class);

    @Autowired private CourseServiceApi courseServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse index(@RequestParam(value = "city") int cityId,
                                   @RequestParam int start,
                                   HttpServletRequest request) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject indexJson = new JSONObject();
        if (start == 0) {
            int platform = getPlatform(request);
            String version = getVersion(request);

            indexJson.put("banners", getBanners(cityId, platform, version));
            indexJson.put("icons", getIcons(cityId, platform, version));
            indexJson.put("events", getEvents(cityId, platform, version));
        }
        indexJson.put("courses", getRecommendCourses(cityId, start));

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    private PagedList<Course> getRecommendCourses(int cityId, int start) {
        try {
            PagedList<Course> courses = courseServiceApi.listRecommend(cityId, start, Configuration.getInt("PageSize.Course"));
            for (Course course : courses.getList()) {
                completeLargeImg(course);
            }

            return courses;
        } catch (Exception e) {
            LOGGER.error("fail to list recommend courses", e);
            return PagedList.EMPTY;
        }
    }
}
