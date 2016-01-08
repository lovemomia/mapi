package cn.momia.mapi.api.teacher;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.course.Course;
import cn.momia.api.course.dto.material.CourseMaterial;
import cn.momia.api.course.dto.course.CourseSku;
import cn.momia.api.course.dto.course.TeacherCourse;
import cn.momia.api.course.dto.course.Student;
import cn.momia.api.user.ChildServiceApi;
import cn.momia.api.user.TeacherServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Child;
import cn.momia.api.user.dto.ChildComment;
import cn.momia.api.user.dto.ChildRecord;
import cn.momia.api.user.dto.ChildTag;
import cn.momia.api.user.dto.Teacher;
import cn.momia.api.user.dto.TeacherStatus;
import cn.momia.api.user.dto.User;
import cn.momia.common.core.dto.PagedList;
import cn.momia.common.core.http.MomiaHttpResponse;
import cn.momia.common.core.util.TimeUtil;
import cn.momia.common.webapp.config.Configuration;
import cn.momia.mapi.api.AbstractApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/v1/teacher")
public class TeacherV1Api extends AbstractApi {
    @Autowired private CourseServiceApi courseServiceApi;
    @Autowired private ChildServiceApi childServiceApi;
    @Autowired private UserServiceApi userServiceApi;
    @Autowired private TeacherServiceApi teacherServiceApi;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public MomiaHttpResponse status(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        TeacherStatus status = teacherServiceApi.status(utoken);
        Teacher teacher = completeTeacherImgs(teacherServiceApi.get(utoken));
        JSONObject statusJson = (JSONObject) JSON.toJSON(teacher);
        statusJson.put("status", status.getStatus());
        statusJson.put("msg", status.getMsg());
        if (teacher.getBirthday() != null) statusJson.put("birthday", TimeUtil.SHORT_DATE_FORMAT.format(teacher.getBirthday()));

        return MomiaHttpResponse.SUCCESS(statusJson);
    }

    private Teacher completeTeacherImgs(Teacher teacher) {
        teacher.setPic(completeSmallImg(teacher.getPic()));
        return teacher;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public MomiaHttpResponse signup(@RequestParam String utoken, @RequestParam String teacher) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(teacher)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.add(utoken, teacher));
    }

    @RequestMapping(value = "/experience", method = RequestMethod.POST)
    public MomiaHttpResponse addExperience(@RequestParam String utoken, @RequestParam String experience) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(experience)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.addExperience(utoken, experience));
    }

    @RequestMapping(value = "/experience", method = RequestMethod.GET)
    public MomiaHttpResponse getExperience(@RequestParam String utoken, @RequestParam int id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.getExperience(utoken, id));
    }

    @RequestMapping(value = "/experience/delete", method = RequestMethod.POST)
    public MomiaHttpResponse addExperience(@RequestParam String utoken, @RequestParam int id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.deleteExperience(utoken, id));
    }

    @RequestMapping(value = "/education", method = RequestMethod.POST)
    public MomiaHttpResponse addEducation(@RequestParam String utoken, @RequestParam String education) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (StringUtils.isBlank(education)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.addEducation(utoken, education));
    }

    @RequestMapping(value = "/education", method = RequestMethod.GET)
    public MomiaHttpResponse getEducation(@RequestParam String utoken, @RequestParam int id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.getEducation(utoken, id));
    }

    @RequestMapping(value = "/education/delete", method = RequestMethod.POST)
    public MomiaHttpResponse addEducation(@RequestParam String utoken, @RequestParam int id) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (id <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.deleteEducation(utoken, id));
    }

    @RequestMapping(value = "/material", method = RequestMethod.GET)
    public MomiaHttpResponse getMaterial(@RequestParam String utoken, @RequestParam(value = "mid") int materialId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (materialId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(completeMaterialImgs(courseServiceApi.getMaterial(utoken, materialId)));
    }

    private CourseMaterial completeMaterialImgs(CourseMaterial material) {
        material.setCover(completeMiddleImg(material.getCover()));
        return material;
    }

    @RequestMapping(value = "/material/list", method = RequestMethod.GET)
    public MomiaHttpResponse listMaterials(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<CourseMaterial> pagedMaterials = courseServiceApi.listMaterials(utoken, start, Configuration.getInt("PageSize.Material"));
        completeMaterialsImgs(pagedMaterials.getList());

        return MomiaHttpResponse.SUCCESS(pagedMaterials);
    }

    private List<CourseMaterial> completeMaterialsImgs(List<CourseMaterial> materials) {
        for (CourseMaterial material : materials) {
            completeMaterialImgs(material);
        }

        return materials;
    }

    @RequestMapping(value = "/course/ongoing", method = RequestMethod.GET)
    public MomiaHttpResponse ongoing(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        JSONObject resultJson = new JSONObject();

        User user = userServiceApi.get(utoken);
        TeacherCourse teacherCourse = courseServiceApi.ongoingTeacherCourse(user.getId());
        if (teacherCourse.exists()) {
            teacherCourse.setCover(completeMiddleImg(teacherCourse.getCover()));
            resultJson.put("course", teacherCourse);

            resultJson.put("students", completeStudentsImgs(courseServiceApi.ongoingStudents(utoken, teacherCourse.getCourseId(), teacherCourse.getCourseSkuId())));
        }

        return MomiaHttpResponse.SUCCESS(resultJson);
    }

    @RequestMapping(value = "/course/notfinished", method = RequestMethod.GET)
    public MomiaHttpResponse notfinished(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        PagedList<TeacherCourse> courses = courseServiceApi.queryNotFinishedByTeacher(user.getId(), start, Configuration.getInt("PageSize.Course"));
        completeMiddleTeacherCoursesImgs(courses.getList());

        return MomiaHttpResponse.SUCCESS(courses);
    }

    private List<TeacherCourse> completeMiddleTeacherCoursesImgs(List<TeacherCourse> teacherCourses) {
        for (TeacherCourse teacherCourse : teacherCourses) {
            teacherCourse.setCover(completeMiddleImg(teacherCourse.getCover()));
        }

        return teacherCourses;
    }

    @RequestMapping(value = "/course/checkin", method = RequestMethod.POST)
    public MomiaHttpResponse checkin(@RequestParam String utoken,
                                     @RequestParam(value = "uid") long userId,
                                     @RequestParam(value = "pid") long packageId,
                                     @RequestParam(value = "coid") long courseId,
                                     @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (userId <= 0 || packageId <= 0 || courseId <= 0 || courseSkuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(courseServiceApi.checkin(utoken, userId, packageId, courseId, courseSkuId));
    }

    @RequestMapping(value = "/course/notfinished/student", method = RequestMethod.GET)
    public MomiaHttpResponse notfinishedStudents(@RequestParam String utoken,
                                                 @RequestParam(value = "coid") long courseId,
                                                 @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (courseId <= 0 || courseSkuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<Student> students = courseServiceApi.notfinishedStudents(utoken, courseId, courseSkuId);
        completeStudentsImgs(students);

        return MomiaHttpResponse.SUCCESS(students);
    }

    private List<Student> completeStudentsImgs(List<Student> students) {
        for (Student student : students) {
            completeStudentImgs(student);
        }

        return students;
    }

    private Student completeStudentImgs(Student student) {
        student.setAvatar(completeSmallImg(student.getAvatar()));
        return student;
    }

    @RequestMapping(value = "/course/finished", method = RequestMethod.GET)
    public MomiaHttpResponse finished(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        User user = userServiceApi.get(utoken);
        PagedList<TeacherCourse> courses = courseServiceApi.queryFinishedByTeacher(user.getId(), start, Configuration.getInt("PageSize.Course"));
        completeMiddleTeacherCoursesImgs(courses.getList());

        return MomiaHttpResponse.SUCCESS(courses);
    }

    @RequestMapping(value = "/course/finished/student", method = RequestMethod.GET)
    public MomiaHttpResponse finishedStudents(@RequestParam String utoken,
                                              @RequestParam(value = "coid") long courseId,
                                              @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (courseId <= 0 || courseSkuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<Student> students = courseServiceApi.finishedStudents(utoken, courseId, courseSkuId);
        completeStudentsImgs(students);

        return MomiaHttpResponse.SUCCESS(students);
    }

    @RequestMapping(value = "/student", method = RequestMethod.GET)
    public MomiaHttpResponse student(@RequestParam String utoken, @RequestParam(value = "cid") long childId, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject studentJson = new JSONObject();

        if (start == 0) {
            Child child = childServiceApi.get(utoken, childId);
            if (!child.exists()) return MomiaHttpResponse.FAILED("孩子信息不存在");

            studentJson.put("child", buildStudent(child));
        }

        PagedList<ChildComment> pagedComments = childServiceApi.listComments(utoken, childId, start, Configuration.getInt("PageSize.ChildComment"));
        studentJson.put("comments", buildStudentComments(pagedComments));

        return MomiaHttpResponse.SUCCESS(studentJson);
    }

    private JSONObject buildStudent(Child child) {
        JSONObject student = new JSONObject();
        student.put("id", child.getId());
        student.put("userId", child.getUserId());
        student.put("avatar", completeSmallImg(child.getAvatar()));
        student.put("name", child.getName());
        student.put("birthday", child.getBirthday());
        student.put("sex", child.getSex());

        return student;
    }

    private PagedList<JSONObject> buildStudentComments(PagedList<ChildComment> pagedComments) {
        Set<Long> teacherUserIds = new HashSet<Long>();
        Set<Long> courseIds = new HashSet<Long>();
        Set<Long> courseSkuIds = new HashSet<Long>();
        for (ChildComment comment : pagedComments.getList()) {
            teacherUserIds.add(comment.getTeacherUserId());
            courseIds.add(comment.getCourseId());
            courseSkuIds.add(comment.getCourseSkuId());
        }

        List<User> teacherUsers = userServiceApi.list(teacherUserIds, User.Type.MINI);
        Map<Long, User> teacherUsersMap = new HashMap<Long, User>();
        for (User user : teacherUsers) {
            teacherUsersMap.put(user.getId(), user);
        }

        List<Course> courses = courseServiceApi.list(courseIds);
        Map<Long, Course> coursesMap = new HashMap<Long, Course>();
        for (Course course : courses) {
            coursesMap.put(course.getId(), course);
        }

        List<CourseSku> skus = courseServiceApi.listSkus(courseSkuIds);
        Map<Long, CourseSku> skusMap = new HashMap<Long, CourseSku>();
        for (CourseSku sku : skus) {
            skusMap.put(sku.getId(), sku);
        }

        List<JSONObject> studentComments = new ArrayList<JSONObject>();
        for (ChildComment comment : pagedComments.getList()) {
            User teacherUser = teacherUsersMap.get(comment.getTeacherUserId());
            Course course = coursesMap.get(comment.getCourseId());
            CourseSku sku = skusMap.get(comment.getCourseSkuId());
            if (teacherUser == null || course == null || sku == null) continue;

            JSONObject studentComment = new JSONObject();
            studentComment.put("date", TimeUtil.SHORT_DATE_FORMAT.format(sku.getStartTime()));
            studentComment.put("title", course.getTitle());
            studentComment.put("content", comment.getContent());
            studentComment.put("teacher", teacherUser.getNickName());

            studentComments.add(studentComment);
        }

        PagedList<JSONObject> pagedStudentComments = new PagedList<JSONObject>();
        pagedStudentComments.setTotalCount(pagedComments.getTotalCount());
        pagedStudentComments.setNextIndex(pagedComments.getNextIndex());
        pagedStudentComments.setList(studentComments);

        return pagedStudentComments;
    }

    @RequestMapping(value = "/student/record", method = RequestMethod.GET)
    public MomiaHttpResponse record(@RequestParam String utoken,
                                    @RequestParam(value = "cid") long childId,
                                    @RequestParam(value = "coid") long courseId,
                                    @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (courseId <= 0 || courseSkuId <= 0 || childId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        Child child = childServiceApi.get(utoken, childId);
        if (!child.exists()) return MomiaHttpResponse.FAILED("孩子信息不存在");

        List<ChildTag> tags = childServiceApi.listAllTags();
        ChildRecord record = childServiceApi.getRecord(utoken, childId, courseId, courseSkuId);

        List<Integer> selectedTags = record.getTags();
        for (ChildTag tag : tags) {
            if (selectedTags.contains(tag.getId())) tag.setSelected(true);
        }

        JSONObject recordJson = new JSONObject();
        recordJson.put("child", buildStudent(child));
        recordJson.put("tags", tags);
        recordJson.put("content", record.getContent());

        return MomiaHttpResponse.SUCCESS(recordJson);
    }

    @RequestMapping(value = "/student/record", method = RequestMethod.POST)
    public MomiaHttpResponse record(@RequestParam String utoken,
                                    @RequestParam(value = "cid") long childId,
                                    @RequestParam(value = "coid") long courseId,
                                    @RequestParam(value = "sid") long courseSkuId,
                                    @RequestParam String record) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (courseId <= 0 || courseSkuId <= 0 || childId <= 0 || StringUtils.isBlank(record)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(childServiceApi.record(utoken, childId, courseId, courseSkuId, record));
    }

    @RequestMapping(value = "/student/comment", method = RequestMethod.POST)
    public MomiaHttpResponse comment(@RequestParam String utoken,
                                     @RequestParam(value = "cid") long childId,
                                     @RequestParam(value = "coid") long courseId,
                                     @RequestParam(value = "sid") long courseSkuId,
                                     @RequestParam String comment) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (courseId <= 0 || courseSkuId <= 0 || childId <= 0 || StringUtils.isBlank(comment)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(childServiceApi.comment(utoken, childId, courseId, courseSkuId, comment));
    }
}
