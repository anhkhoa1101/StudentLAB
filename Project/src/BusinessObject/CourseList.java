package BusinessObject;

import Core.Entities.Course;
import Core.Entities.Student;
import Core.Interfaces.ICourseDAO;
import Core.Interfaces.IStudentDAO;
import Utilities.Validation.CourseValidation;

import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

public class CourseList {

    private final ICourseDAO courseDAO;
    private final IStudentDAO studentDAO;
    private boolean dirty = false;

    public CourseList(ICourseDAO courseDAO, IStudentDAO studentDAO) {
        this.courseDAO = courseDAO;
        this.studentDAO = studentDAO;
    }

    public void add(String courseId, String studentId, String courseName, int duration, Date startDate) {

        if (!CourseValidation.isNotEmpty(courseId))
            throw new IllegalArgumentException("Course ID cannot be empty!");
        Student s = studentDAO.findByID(studentId).orElseThrow(() -> new IllegalArgumentException("Student ID does not exist!"));
        if (!CourseValidation.isNotEmpty(courseName))
            throw new IllegalArgumentException("Course name cannot be empty!");
        if (!CourseValidation.isValidDuration(duration))
            throw new IllegalArgumentException("Duration must be at least 1 week!");
        if (!CourseValidation.isFutureDate(startDate))
            throw new IllegalArgumentException("Start date must be a future date!");

        Course c = new Course(courseId.trim(), s, courseName.trim(), duration, startDate);
        if (!courseDAO.add(c))
            throw new IllegalArgumentException("Course ID already exists!");
        dirty = true;
    }

/**
 * Hiển thị tất cả khóa học dựa theo sinh viên
 * */
    public List<Course> listGroupedByStudent() {
        List<Course> list = courseDAO.readAll();
        list.sort(Comparator.comparing((Course c) -> c.getStudent().getId())
                .thenComparing(Course::getCourseId));
        return list;
    }

    public int totalDuration(String studentId) {
        Student s = studentDAO.findByID(studentId).orElseThrow(() -> new IllegalArgumentException("Student ID does not exist!"));
        int sum = 0;
        for (Course c : courseDAO.findByStudentId(s.getId())) sum += c.getDuration();
        return sum;
    }

    public boolean isDirty() { return dirty; }
    public Map<String, Long> courseCountByStudent() {
        return courseDAO.readAll().stream().collect(Collectors.groupingBy(
                c -> c.getStudent().getId(), TreeMap::new, Collectors.counting()));
    }


    public boolean save() {
        boolean ok = courseDAO.save();
        if (ok) dirty = false;
        return ok;
    }
}