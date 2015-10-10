package cn.momia.mapi.api.v1.course;

import cn.momia.common.api.http.MomiaHttpResponse;
import cn.momia.mapi.api.v1.AbstractV1Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subject")
public class SubjectV1Api extends AbstractV1Api {
    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam long id) {
        return null;
    }
}
