package cn.momia.mapi.api.course;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.OrderServiceApi;
import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.comment.UserCourseComment;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.course.CourseDetail;
import cn.momia.api.course.dto.course.CourseSku;
import cn.momia.api.course.dto.course.CourseSkuPlace;
import cn.momia.api.course.dto.subject.SubjectSku;
import cn.momia.api.poi.PoiServiceApi;
import cn.momia.api.poi.dto.Place;
import cn.momia.api.user.TeacherServiceApi;
import cn.momia.api.user.dto.Teacher;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v3/course")
public class CourseV3Api extends AbstractApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseV3Api.class);

    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private SubjectServiceApi subjectServiceApi;
    @Autowired private OrderServiceApi orderServiceApi;
    @Autowired private PoiServiceApi poiServiceApi;
    @Autowired private TeacherServiceApi teacherServiceApi;

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam(required = false, defaultValue = "") String utoken,
                                 @RequestParam long id,
                                 @RequestParam(required = false, defaultValue = "") String pos,
                                 @RequestParam(required = false, defaultValue = "0") int recommend,
                                 @RequestParam(required = false, defaultValue = "0") int trial,
                                 @RequestParam(value = "sid", required = false, defaultValue = "0") long courseSkuId) {
        if (id <= 0) return MomiaHttpResponse.FAILED("无效的课程ID");

        Course course = completeLargeImg(courseServiceApi.get(id, pos));
        JSONObject courseJson = (JSONObject) JSON.toJSON(course);

        PagedList<Integer> pagedTeacherIds = courseServiceApi.teacherIds(id, 0, Configuration.getInt("PageSize.Teacher"));
        List<Teacher> teachers = completeTeachersImgs(teacherServiceApi.list(pagedTeacherIds.getList()));
        if (!teachers.isEmpty()) courseJson.put("teachers", teachers);

        PagedList<UserCourseComment> pagedComments = courseServiceApi.queryCommentsByCourse(id, 0, Configuration.getInt("PageSize.CourseComment"));
        completeCourseCommentsImgs(pagedComments.getList());
        if (!pagedComments.getList().isEmpty()) courseJson.put("comments", pagedComments);

        List<SubjectSku> subjectSkus = subjectServiceApi.querySkus(course.getSubjectId());
        SubjectSku cheapestSubjectSku = null;
        for (SubjectSku subjectSku : subjectSkus) {
            if (subjectSku.getCourseId() > 0) continue;
            if (cheapestSubjectSku == null || subjectSku.getPrice().compareTo(cheapestSubjectSku.getPrice()) < 0) cheapestSubjectSku = subjectSku;
        }

        if (cheapestSubjectSku != null) {
            courseJson.put("cheapestSkuPrice", cheapestSubjectSku.getPrice());
            courseJson.put("cheapestSkuTimeUnit", TimeUtil.toUnitString(cheapestSubjectSku.getTimeUnit()));
            courseJson.put("cheapestSkuDesc", "任选" + MomiaUtil.CHINESE_NUMBER_CHARACTER[cheapestSubjectSku.getCourseCount()] + "门");
        }

        if (recommend == 1) {
            JSONArray subjectNoticeJson = new JSONArray();
            JSONObject noticeJson = new JSONObject();
            noticeJson.put("title", "购买须知");
            noticeJson.put("content", course.getNotice());
            subjectNoticeJson.add(noticeJson);
            courseJson.put("subjectNotice", subjectNoticeJson);
        }

        try {
            CourseDetail detail = courseServiceApi.detail(id);
            JSONArray detailJson = JSON.parseArray(detail.getDetail());
            for (int i = 0; i < detailJson.size(); i++) {
                JSONObject detailBlockJson = detailJson.getJSONObject(i);
                JSONArray contentJson = detailBlockJson.getJSONArray("content");
                for (int j = 0; j < contentJson.size(); j++) {
                    JSONObject contentBlockJson = contentJson.getJSONObject(j);
                    if (contentBlockJson.containsKey("img")) contentBlockJson.put("img", completeLargeImg(contentBlockJson.getString("img")));
                }
            }
            courseJson.put("goal", detail.getAbstracts());
            courseJson.put("detail", detailJson);
        } catch (Exception e) {
            LOGGER.warn("invalid course detail: {}", id);
        }

        if (recommend != 1 && trial != 1) {
            courseJson.put("buyable", false);
            courseJson.put("status", 1);
        }

        if (courseSkuId > 0) {
            CourseSku sku = courseServiceApi.getSku(id, courseSkuId);
            Place place = poiServiceApi.getPlace(sku.getPlaceId());
            CourseSkuPlace skuPlace = new CourseSkuPlace();
            skuPlace.setId(place.getId());
            skuPlace.setCityId(place.getCityId());
            skuPlace.setRegionId(place.getRegionId());
            skuPlace.setAddress(place.getAddress());
            skuPlace.setName(place.getName());
            skuPlace.setLng(place.getLng());
            skuPlace.setLat(place.getLat());
            skuPlace.setRoute(sku.getRoute());
            skuPlace.setScheduler(sku.getScheduler());
            courseJson.put("place", skuPlace);
        }

        if (!StringUtils.isBlank(utoken)) {
            courseJson.put("packageId", orderServiceApi.bookablePackageId(utoken, id));
        } else {
            courseJson.put("packageId", 0);
        }

        return MomiaHttpResponse.SUCCESS(courseJson);
    }
}
