package cn.momia.mapi.api.expert;

import cn.momia.api.course.ExpertServiceApi;
import cn.momia.api.course.expert.Expert;
import cn.momia.api.course.expert.ExpertBanner;
import cn.momia.api.course.expert.ExpertCourse;
import cn.momia.api.course.expert.ExpertCoursePage;
import cn.momia.api.course.expert.ExpertQuestion;
import cn.momia.api.course.expert.ExpertQuestionPage;
import cn.momia.api.course.expert.HearResult;
import cn.momia.api.course.expert.MyCentre;
import cn.momia.api.course.expert.QExpert;
import cn.momia.api.course.expert.UserAsset;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.MomiaUtil;
import cn.momia.mapi.api.AbstractApi;
import cn.momia.mapi.api.index.AbstractIndexApi;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoze on 16/6/16.
 */
@RestController
@RequestMapping("/v1")
public class ExpertV1Api extends AbstractApi {

    private static final Logger log = LoggerFactory.getLogger(ExpertV1Api.class);

    @Autowired
    private ExpertServiceApi expertServiceApi;

    @Autowired
    private UserServiceApi userServiceApi;

    /**
     * 首页
     * @param request
     * @return
     */
    @RequestMapping(value = "/wd_home", method = RequestMethod.GET)
    public MomiaHttpResponse index(HttpServletRequest request) {
        JSONObject indexJson = new JSONObject();
        List<ExpertBanner> banners = expertServiceApi.banners();
        for (ExpertBanner banner : banners){
            banner.setCover(completeImg(banner.getCover()));
        }
        indexJson.put("banners", banners);

        List<ExpertCourse> courses = expertServiceApi.courses();
        for (ExpertCourse course : courses){
            course.setCover(completeImg(course.getCover()));
            course.setContent(completeImg(course.getContent()));
            Expert expert = course.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
        }
        indexJson.put("wdcourses", courses);

        List<ExpertQuestion> questions = expertServiceApi.questions(0);
        for (ExpertQuestion question : questions){
            Expert expert = question.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
        }
        indexJson.put("questions", questions);

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    /**
     * 课程列表
     * @param request
     * @param wid
     * @param start
     * @return
     */
    @RequestMapping(value = "/wd_courses", method = RequestMethod.GET)
    public MomiaHttpResponse courses(HttpServletRequest request ,@RequestParam(value = "wid") int wid,@RequestParam(value = "start") int start) {
        ExpertCoursePage page = expertServiceApi.coursePage(wid,start);
        List<ExpertCourse> courses = page.getList();
        for (ExpertCourse course : courses){
            course.setCover(completeImg(course.getCover()));
            course.setContent(completeImg(course.getContent()));
            Expert expert = course.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
        }
        page.setList(courses);

        return MomiaHttpResponse.SUCCESS(page);
    }

    /**
     * 问题列表
     * @param request
     * @param wid
     * @param start
     * @return
     */
    @RequestMapping(value = "/wd_questions", method = RequestMethod.GET)
    public MomiaHttpResponse questions(HttpServletRequest request ,@RequestParam(value = "wid") int wid,@RequestParam(value = "start") int start) {
        ExpertQuestionPage page = expertServiceApi.questionPage(wid, start);
        List<ExpertQuestion> questions = page.getList();
        for (ExpertQuestion question : questions){
            Expert expert = question.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
            User user = userServiceApi.get(question.getUserId());
            String userAvatar = "";
            if (user != null) {
                if (StringUtils.isNotEmpty(user.getAvatar())){
                    userAvatar = completeImg(user.getAvatar());
                }else{
                    if (StringUtils.isNotEmpty(user.getCover())){
                        userAvatar = completeImg(user.getCover());
                    }
                }
            }
            question.setUserName(user.getNickName());
            question.setUserAvatar(userAvatar);
        }
        page.setList(questions);

        return MomiaHttpResponse.SUCCESS(page);
    }

    /**
     * 微课详情
     * @param request
     * @param wid
     * @param start
     * @return
     */
    @RequestMapping(value = "/wd_courseDetails", method = RequestMethod.GET)
    public MomiaHttpResponse courseDetails(HttpServletRequest request ,@RequestParam(value = "wid") int wid,@RequestParam(value = "start") int start) {

        JSONObject indexJson = new JSONObject();

        ExpertCourse expertCourse = expertServiceApi.get(wid);

        expertCourse.setCover(completeImg(expertCourse.getCover()));
        expertCourse.setContent(completeImg(expertCourse.getContent()));
        Expert expert_1 = expertCourse.getExpert();
        if (expert_1 != null) expert_1.setCover(completeImg(expert_1.getCover()));
        indexJson.put("wdcourse", expertCourse);

        ExpertCoursePage page1 = expertServiceApi.coursePage((int)expertCourse.getExpertId(),start);
        List<ExpertCourse> courses = page1.getList();
        for (ExpertCourse course : courses){
            course.setCover(completeImg(course.getCover()));
            course.setContent(completeImg(course.getContent()));
            Expert expert = course.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
        }
        page1.setList(courses);
        indexJson.put("wdcourses", page1);

        List<ExpertQuestion> questions = expertServiceApi.questions(expertCourse.getId());
        for (ExpertQuestion question : questions){
            Expert expert = question.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
            User user = userServiceApi.get(question.getUserId());
            String userAvatar = "";
            if (user != null) {
                if (StringUtils.isNotEmpty(user.getAvatar())){
                    userAvatar = completeImg(user.getAvatar());
                }else{
                    if (StringUtils.isNotEmpty(user.getCover())){
                        userAvatar = completeImg(user.getCover());
                    }
                }
            }
            question.setUserName(user.getNickName());
            question.setUserAvatar(userAvatar);
        }
        indexJson.put("questions", questions);

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    /**
     * 课程信息
     * @param request
     * @param wid
     * @return
     */
    @RequestMapping(value = "/wd_course", method = RequestMethod.GET)
    public MomiaHttpResponse course(HttpServletRequest request ,@RequestParam(value = "wid") int wid) {
        JSONObject indexJson = new JSONObject();
        ExpertCourse expertCourse = expertServiceApi.get(wid);

        expertCourse.setCover(completeImg(expertCourse.getCover()));
        expertCourse.setContent(completeImg(expertCourse.getContent()));
        Expert expert_1 = expertCourse.getExpert();
        if (expert_1 != null) expert_1.setCover(completeImg(expert_1.getCover()));
        indexJson.put("wdcourse", expertCourse);

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    @RequestMapping(value = "/wd_qExpert", method = RequestMethod.GET)
    public MomiaHttpResponse qExpert(HttpServletRequest request ,@RequestParam(value = "qid") int qid) {
        JSONObject indexJson = new JSONObject();
        QExpert qExpert = expertServiceApi.questionExpert(qid);
        ExpertQuestion question = qExpert.getQuestion();
        question.setAnswer("");
        User user = userServiceApi.get(question.getUserId());
        String userAvatar = "";
        if (user != null) {
            if (StringUtils.isNotEmpty(user.getAvatar())){
                userAvatar = completeImg(user.getAvatar());
            }else{
                if (StringUtils.isNotEmpty(user.getCover())){
                    userAvatar = completeImg(user.getCover());
                }
            }
        }
        question.setUserName(user.getNickName());
        question.setUserAvatar(userAvatar);

        ExpertCourse expertCourse = qExpert.getCourse();
        expertCourse.setCover(completeImg(expertCourse.getCover()));
        expertCourse.setContent(completeImg(expertCourse.getContent()));


        Expert expert = qExpert.getExpert();
        if (expert != null) expert.setCover(completeImg(expert.getCover()));
        question.setExpert(expert);
        expertCourse.setExpert(expert);
        indexJson.put("course", expertCourse);
        indexJson.put("question", question);
        indexJson.put("expert", expert);

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    @RequestMapping(value = "/wd_qJoin", method = RequestMethod.GET)
    public MomiaHttpResponse questionJoin(HttpServletRequest request,@RequestParam(value = "courseId") int courseId,
                                  @RequestParam String utoken,
                                  @RequestParam(value = "content") String content) {
        if (courseId <= 0) return MomiaHttpResponse.FAILED("无效的微课信息");
        log.info("content====="+content);

        return MomiaHttpResponse.SUCCESS(expertServiceApi.questionJoin(courseId, utoken, content));
    }

    @RequestMapping(value = "/wd_qAnswer", method = RequestMethod.GET)
    public MomiaHttpResponse qAnswer(HttpServletRequest request,@RequestParam(value = "questionId") int questionId,
                                          @RequestParam(value = "answer") String answer,
                                          @RequestParam int mins) {

        return MomiaHttpResponse.SUCCESS(expertServiceApi.questionAnswer(questionId, answer, mins));
    }

    @RequestMapping(value = "/wd_hJoin", method = RequestMethod.GET)
    public MomiaHttpResponse hearJoin(HttpServletRequest request, @RequestParam(value = "qid") int qid,@RequestParam String utoken) {
        if (qid <= 0) return MomiaHttpResponse.FAILED("无效的问题信息");
        JSONObject indexJson = new JSONObject();
        HearResult result = expertServiceApi.hearJoin(qid, utoken);
        if (result.getStatus() == 1){
            indexJson.put("order", result.getOrder());
        }else{
            ExpertQuestion question = result.getQuestion();
            if (question.exists()) question.setAnswer(completeImg(question.getAnswer()));
            if (question.getExpert() != null) question.getExpert().setCover(completeImg(question.getExpert().getCover()));
            indexJson.put("question", question);
        }
        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    @RequestMapping(value = "/prepay/wd_asset", method = RequestMethod.POST)
    public MomiaHttpResponse prepayAsset(@RequestParam(value = "oid") long orderId, @RequestParam(defaultValue = "app") String type) {
        if (orderId <= 0) return MomiaHttpResponse.FAILED("无效的订单ID");
        if (StringUtils.isBlank(type)) return MomiaHttpResponse.FAILED("无效的类型");

        return MomiaHttpResponse.SUCCESS(expertServiceApi.prepayAsset(orderId, type));
    }

    @RequestMapping(value = "/prepay/wd_alipay", method = RequestMethod.POST)
    public MomiaHttpResponse prepayAlipay(@RequestParam(value = "oid") long orderId, @RequestParam(defaultValue = "app") String type) {
        if (orderId <= 0) return MomiaHttpResponse.FAILED("无效的订单ID");
        if (StringUtils.isBlank(type)) return MomiaHttpResponse.FAILED("无效的类型");

        return MomiaHttpResponse.SUCCESS(expertServiceApi.prepayAlipay(orderId, type));
    }

    @RequestMapping(value = "/prepay/wd_weixin", method = RequestMethod.POST)
    public MomiaHttpResponse prepayWeixin(@RequestParam(value = "oid") long orderId,
                                          @RequestParam(defaultValue = "app") final String type,
                                          @RequestParam(required = false) String code) {
        if (orderId <= 0) return MomiaHttpResponse.FAILED("无效的订单ID");
        if (StringUtils.isBlank(type)) return MomiaHttpResponse.FAILED("无效的类型");

        return MomiaHttpResponse.SUCCESS(expertServiceApi.prepayWeixin(orderId, type, code));
    }

    @RequestMapping(value = "/wd_check", method = RequestMethod.POST)
    public MomiaHttpResponse check(@RequestParam(value = "oid") long orderId) {
        if (orderId <= 0) return MomiaHttpResponse.FAILED("无效的订单ID");
        return MomiaHttpResponse.SUCCESS(expertServiceApi.checkPayment(orderId));
    }

    @RequestMapping(value = "/wd_centre", method = RequestMethod.GET)
    public MomiaHttpResponse myCentre(HttpServletRequest request,@RequestParam(value = "utoken") String utoken) {
//        log.info("获取我的中心数据>>>>>>>>>>>"+utoken);
        MyCentre entity = expertServiceApi.myCentre(utoken);
        return MomiaHttpResponse.SUCCESS(entity);
    }

    @RequestMapping(value = "/wd_myQuestion", method = RequestMethod.GET)
    public MomiaHttpResponse myQuestionPages(HttpServletRequest request,@RequestParam(value = "utoken") String utoken,
                                             @RequestParam(value = "start") int start) {
        ExpertQuestionPage page = expertServiceApi.myQuestionPages(utoken, start);
        List<ExpertQuestion> questions = page.getList();
        for (ExpertQuestion question : questions){
            Expert expert = question.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
            User user = userServiceApi.get(question.getUserId());
            String userAvatar = "";
            if (user != null) {
                if (StringUtils.isNotEmpty(user.getAvatar())){
                    userAvatar = completeImg(user.getAvatar());
                }else{
                    if (StringUtils.isNotEmpty(user.getCover())){
                        userAvatar = completeImg(user.getCover());
                    }
                }
            }
            question.setUserName(user.getNickName());
            question.setUserAvatar(userAvatar);
        }
        page.setList(questions);

        return MomiaHttpResponse.SUCCESS(page);
    }

    @RequestMapping(value = "/wd_myAnswer", method = RequestMethod.GET)
    public MomiaHttpResponse myAnswerPages(HttpServletRequest request,@RequestParam(value = "utoken") String utoken,
                                             @RequestParam(value = "start") int start) {
        JSONObject indexJson = new JSONObject();
        ExpertQuestionPage page_y = expertServiceApi.myAnswerPages(utoken, start);
        List<ExpertQuestion> questions = page_y.getList();
        for (ExpertQuestion question : questions){
            Expert expert = question.getExpert();
            if (expert != null) expert.setCover(completeImg(expert.getCover()));
            User user = userServiceApi.get(question.getUserId());
            String userAvatar = "";
            if (user != null) {
                if (StringUtils.isNotEmpty(user.getAvatar())){
                    userAvatar = completeImg(user.getAvatar());
                }else{
                    if (StringUtils.isNotEmpty(user.getCover())){
                        userAvatar = completeImg(user.getCover());
                    }
                }
            }
            question.setUserName(user.getNickName());
            question.setUserAvatar(userAvatar);
        }
        page_y.setList(questions);

        ExpertQuestionPage page_n = expertServiceApi.myAnswerNullPages(utoken, start);
        List<ExpertQuestion> questions_n = page_n.getList();
        for (ExpertQuestion question : questions_n){
            Expert expert_n = question.getExpert();
            if (expert_n != null) expert_n.setCover(completeImg(expert_n.getCover()));
            User user = userServiceApi.get(question.getUserId());
            String userAvatar = "";
            if (user != null) {
                if (StringUtils.isNotEmpty(user.getAvatar())){
                    userAvatar = completeImg(user.getAvatar());
                }else{
                    if (StringUtils.isNotEmpty(user.getCover())){
                        userAvatar = completeImg(user.getCover());
                    }
                }
            }
            question.setUserName(user.getNickName());
            question.setUserAvatar(userAvatar);
        }
        page_n.setList(questions_n);

        indexJson.put("allAnswer", page_y);
        indexJson.put("nullAnswer", page_n);

        return MomiaHttpResponse.SUCCESS(indexJson);
    }

    @RequestMapping(value = "/wd_myAsset", method = RequestMethod.GET)
    public MomiaHttpResponse myUserAsset(HttpServletRequest request,@RequestParam(value = "utoken") String utoken) {
        UserAsset asset = expertServiceApi.myUserAsset(utoken);
        return MomiaHttpResponse.SUCCESS(asset);
    }

    @RequestMapping(value = "/wd_tCourseCount", method = RequestMethod.GET)
    public MomiaHttpResponse courseCount(HttpServletRequest request,@RequestParam(value = "wid") int wid) {
        log.info("mapi>>>>>>>>>>>"+wid);
        return MomiaHttpResponse.SUCCESS(expertServiceApi.course_count(wid));
    }


}