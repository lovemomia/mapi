package cn.momia.mapi.api.v2.course;

import cn.momia.api.course.SubjectServiceApi;
import cn.momia.api.course.dto.SubjectDto;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.image.api.ImageFile;
import cn.momia.mapi.api.v2.AbstractV2Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/subject")
public class SubjectV2Api extends AbstractV2Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectV2Api.class);

    @Autowired private SubjectServiceApi subjectServiceApi;

    @RequestMapping(value = "/trial", method = RequestMethod.GET)
    public MomiaHttpResponse trial(@RequestParam(value = "city") int cityId, @RequestParam int start) {
        if (cityId < 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;
        return MomiaHttpResponse.SUCCESS(getTrialSubjects(cityId, start));
    }

    private PagedList<SubjectDto> getTrialSubjects(int cityId, int start) {
        try {
            PagedList<SubjectDto> subjects = subjectServiceApi.listTrial(cityId, start, Configuration.getInt("PageSize.TrialSubject"));
            for (SubjectDto subject : subjects.getList()) {
                subject.setCover(ImageFile.largeUrl(subject.getCover()));
            }

            return subjects;
        } catch (Exception e) {
            LOGGER.error("fail to list trial subjects", e);
            return PagedList.EMPTY;
        }
    }
}
