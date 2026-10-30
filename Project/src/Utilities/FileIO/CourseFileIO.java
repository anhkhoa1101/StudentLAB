package Utilities.FileIO;

import Core.Entities.Course;
import Core.Entities.Student;
import Utilities.Validation.CourseValidation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CourseFileIO extends FileHelper<Course> {

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private final Map<String, Student> students; // key = studentId

    public CourseFileIO(String path, Map<String, Student> students) {
        super(path);
        this.students = students;
        this.df.setLenient(false); // 31/02/2025 -> lỗi
    }

    // courseId, studentId, courseName, duration, startDate
    @Override
    protected Course parse(String line) throws Exception {
        String[] t = line.split(",");
        if (t.length != 5) {
            throw new Exception("expected 5 fields, got " + t.length);
        }

        String courseId   = t[0].trim();
        String studentId  = t[1].trim();
        String courseName = t[2].trim();
        int duration      = Integer.parseInt(t[3].trim());
        Date startDate    = df.parse(t[4].trim());

        if (courseId.isEmpty() || courseName.isEmpty()) {
            throw new Exception("Empty courseId/courseName");
        }
        if (duration < CourseValidation.MIN_DURATION || duration > CourseValidation.MAX_DURATION) {
            throw new Exception("Duration must be " + CourseValidation.MIN_DURATION + "-" + CourseValidation.MAX_DURATION);
        }

        Student s = students.get(studentId);
        if (s == null) {
            throw new Exception("Student not found: " + studentId);
        }

        return new Course(courseId, s, courseName, duration, startDate);
    }

    @Override
    protected String format(Course c) {
        return c.toDataString();
    }
}