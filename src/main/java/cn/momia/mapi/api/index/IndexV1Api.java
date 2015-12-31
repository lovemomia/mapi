package cn.momia.mapi.api.index;

import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.Subject;
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
@RequestMapping("/v1/index")
public class IndexV1Api extends AbstractIndexApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexV1Api.class);

    @Autowired private SubjectServiceApi subjectServiceApi;

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
        indexJson.put("subjects", getTrialSubjects(cityId, start));

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    private PagedList<Subject> getTrialSubjects(int cityId, int start) {
        try {
            PagedList<Subject> subjects = subjectServiceApi.listTrial(cityId, start, Configuration.getInt("PageSize.Trial"));
            for (Subject subject : subjects.getList()) {
                completeLargeImg(subject);
            }

            return subjects;
        } catch (Exception e) {
            LOGGER.error("fail to list trial subjects", e);
            return PagedList.EMPTY;
        }
    }
}
