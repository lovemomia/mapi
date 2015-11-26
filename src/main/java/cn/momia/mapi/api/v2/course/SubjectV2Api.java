package cn.momia.mapi.api.v2.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.CourseDto;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.api.course.dto.SubjectSkuDto;
import cn.momia.api.feed.FeedServiceApi;
import cn.momia.api.feed.dto.FeedDto;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.ContactDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v2.AbstractV2Api;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v2/subject")
public class SubjectV2Api extends AbstractV2Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectV2Api.class);

    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private FeedServiceApi feedServiceApi;
    @Autowired private UserServiceApi userServiceApi;

    @RequestMapping(value = "/trial", method = RequestMethod.GET)
    public MomiaHttpResponse trial(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(getTrialSubjects(cityId, start));
    }

    private PagedList<CourseDto> getTrialSubjects(int cityId, int start) {
        try {
            PagedList<CourseDto> courses = courseServiceApi.listTrial(cityId, start, Configuration.getInt("PageSize.Trial"));
            for (CourseDto course : courses.getList()) {
                course.setCover(ImageFile.largeUrl(course.getCover()));
            }

            return courses;
        } catch (Exception e) {
            LOGGER.error("fail to list trial courses", e);
            return PagedList.EMPTY;
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(required = false, defaultValue = "") String utoken, @RequestParam long id) {
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        SubjectDto subject = subjectServiceApi.get(id);
        subject.setCover(ImageFile.largeUrl(subject.getCover()));
        subject.setImgs(completeLargeImgs(subject.getImgs()));

        PagedList<CourseDto> courses = courseServiceApi.query(id, 0, 10);
        processCourses(courses.getList());

        long userId = StringUtils.isBlank(utoken) ? 0 : userServiceApi.get(utoken).getId();
        PagedList<FeedDto> feeds = feedServiceApi.queryBySubject(userId, id, 0, 10);
        processFeeds(feeds.getList());

        JSONObject responseJson = new JSONObject();
        responseJson.put("subject", subject);
        responseJson.put("courses", courses);
        responseJson.put("feeds", feeds);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }

    @RequestMapping(value = "/sku", method = RequestMethod.GET)
    public MomiaHttpResponse sku(@RequestParam String utoken, @RequestParam long id, @RequestParam(required = false, value = "coid", defaultValue = "0") long courseId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<SubjectSkuDto> skus = subjectServiceApi.querySkus(id);
        ContactDto contact = userServiceApi.getContact(utoken);

        List<SubjectSkuDto> courseSkus = new ArrayList<SubjectSkuDto>();
        List<SubjectSkuDto> subjectSkus = new ArrayList<SubjectSkuDto>();
        for (SubjectSkuDto sku : skus) {
            if (sku.getCourseId() <= 0) {
                subjectSkus.add(sku);
            } else if (sku.getCourseId() == courseId) {
                courseSkus.add(sku);
            }
        }

        JSONObject responseJson = new JSONObject();
        if (courseId > 0) {
            responseJson.put("skus", courseSkus);
            responseJson.put("packages", subjectSkus);
        } else {
            responseJson.put("skus", subjectSkus);
        }
        responseJson.put("contact", contact);

        return MomiaHttpResponse.SUCCESS(responseJson);
    }
}
