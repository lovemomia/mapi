package cn.momia.mapi.api.v1.teacher;

import cn.momia.api.course.CourseServiceApi;
import cn.momia.api.course.dto.TeacherCourse;
import cn.momia.api.teacher.TeacherServiceApi;
import cn.momia.api.teacher.dto.ChildComment;
import cn.momia.api.teacher.dto.ChildRecord;
import cn.momia.api.teacher.dto.ChildTag;
import cn.momia.api.teacher.dto.Material;
import cn.momia.api.teacher.dto.Student;
import cn.momia.api.teacher.dto.Teacher;
import cn.momia.api.teacher.dto.TeacherStatus;
import cn.momia.api.user.ChildServiceApi;
import cn.momia.api.user.UserServiceApi;
import cn.momia.api.user.dto.Child;
import cn.momia.api.user.dto.User;
import cn.momia.common.api.dto.PagedList;
import cn.momia.common.api.http.MomiaHttpResponse;
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

import java.util.List;

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
        if (status.getStatus() == TeacherStatus.Status.NOT_EXIST) return MomiaHttpResponse.SUCCESS(status);

        Teacher teacher = completeTeacherImgs(teacherServiceApi.get(utoken));
        JSONObject statusJson = (JSONObject) JSON.toJSON(teacher);
        statusJson.put("status", status.getStatus());
        statusJson.put("msg", status.getMsg());

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

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.signup(utoken, teacher));
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

    @RequestMapping(method = RequestMethod.GET)
    public MomiaHttpResponse get(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        return MomiaHttpResponse.SUCCESS(completeTeacherImgs(teacherServiceApi.get(utoken)));
    }

    @RequestMapping(value = "/material", method = RequestMethod.GET)
    public MomiaHttpResponse getMaterial(@RequestParam String utoken, @RequestParam(value = "mid") int materialId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (materialId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(completeMaterialImgs(teacherServiceApi.getMaterial(utoken, materialId)));
    }

    private Material completeMaterialImgs(Material material) {
        material.setCover(completeMiddleImg(material.getCover()));
        return material;
    }

    @RequestMapping(value = "/material/list", method = RequestMethod.GET)
    public MomiaHttpResponse listMaterials(@RequestParam String utoken, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (start < 0) return MomiaHttpResponse.BAD_REQUEST;

        PagedList<Material> pagedMaterials = teacherServiceApi.listMaterials(utoken, start, Configuration.getInt("PageSize.Material"));
        completeMaterialsImgs(pagedMaterials.getList());

        return MomiaHttpResponse.SUCCESS(pagedMaterials);
    }

    private List<Material> completeMaterialsImgs(List<Material> materials) {
        for (Material material : materials) {
            completeMaterialImgs(material);
        }

        return materials;
    }

    @RequestMapping(value = "/course/ongoing", method = RequestMethod.GET)
    public MomiaHttpResponse ongoing(@RequestParam String utoken) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;

        JSONObject resultJson = new JSONObject();

        User user = userServiceApi.get(utoken);
        TeacherCourse teacherCourse = courseServiceApi.getOngoingTeacherCourse(user.getId());
        if (teacherCourse.exists()) {
            teacherCourse.setCover(completeMiddleImg(teacherCourse.getCover()));
            resultJson.put("course", teacherCourse);

            resultJson.put("students", completeStudentsImgs(teacherServiceApi.ongoingStudents(utoken, teacherCourse.getCourseId(), teacherCourse.getCourseSkuId())));
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

    @RequestMapping(value = "/course/notfinished/student", method = RequestMethod.GET)
    public MomiaHttpResponse notfinishedStudents(@RequestParam String utoken,
                                                 @RequestParam(value = "coid") long courseId,
                                                 @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (courseId <= 0 || courseSkuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        List<Student> students = teacherServiceApi.notfinishedStudents(utoken, courseId, courseSkuId);
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

        List<Student> students = teacherServiceApi.finishedStudents(utoken, courseId, courseSkuId);
        completeStudentsImgs(students);

        return MomiaHttpResponse.SUCCESS(students);
    }

    @RequestMapping(value = "/course/checkin", method = RequestMethod.POST)
    public MomiaHttpResponse checkin(@RequestParam String utoken,
                                     @RequestParam(value = "uid") long userId,
                                     @RequestParam(value = "pid") long packageId,
                                     @RequestParam(value = "coid") long courseId,
                                     @RequestParam(value = "sid") long courseSkuId) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (userId <= 0 || packageId <= 0 || courseId <= 0 || courseSkuId <= 0) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.checkin(utoken, userId, packageId, courseId, courseSkuId));
    }

    @RequestMapping(value = "/student", method = RequestMethod.GET)
    public MomiaHttpResponse student(@RequestParam String utoken, @RequestParam(value = "cid") long childId, @RequestParam int start) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (childId <= 0 || start < 0) return MomiaHttpResponse.BAD_REQUEST;

        JSONObject studentJson = new JSONObject();

        if (start == 0) {
            Child child = childServiceApi.get(utoken, childId);
            if (!child.exists()) return MomiaHttpResponse.FAILED("孩子信息不存在");

            studentJson.put("child", completeStudentImgs(buildStudent(child)));
        }

        PagedList<ChildComment> comments = teacherServiceApi.listChildComments(utoken, childId, start, Configuration.getInt("PageSize.ChildComment"));
        studentJson.put("comments", comments);

        return MomiaHttpResponse.SUCCESS(studentJson);
    }

    private Student buildStudent(Child child) {
        Student student = new Student();
        student.setId(child.getId());
        student.setUserId(child.getUserId());
        student.setAvatar(child.getAvatar());
        student.setName(child.getName());
        student.setBirthday(child.getBirthday());
        student.setSex(child.getSex());

        return student;
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

        List<ChildTag> tags = teacherServiceApi.listAllChildTags();
        ChildRecord record = teacherServiceApi.getChildRecord(utoken, childId, courseId, courseSkuId);

        JSONObject recordJson = new JSONObject();
        recordJson.put("child", completeStudentImgs(buildStudent(child)));
        recordJson.put("tags", tags);
        recordJson.put("record", record);

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

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.record(utoken, childId, courseId, courseSkuId, record));
    }

    @RequestMapping(value = "/student/comment", method = RequestMethod.POST)
    public MomiaHttpResponse comment(@RequestParam String utoken,
                                     @RequestParam(value = "cid") long childId,
                                     @RequestParam(value = "coid") long courseId,
                                     @RequestParam(value = "sid") long courseSkuId,
                                     @RequestParam String comment) {
        if (StringUtils.isBlank(utoken)) return MomiaHttpResponse.TOKEN_EXPIRED;
        if (courseId <= 0 || courseSkuId <= 0 || childId <= 0 || StringUtils.isBlank(comment)) return MomiaHttpResponse.BAD_REQUEST;

        return MomiaHttpResponse.SUCCESS(teacherServiceApi.comment(utoken, childId, courseId, courseSkuId, comment));
    }
}
