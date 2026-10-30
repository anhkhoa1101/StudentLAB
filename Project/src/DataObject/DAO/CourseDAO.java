package DataObject.DAO;

import Core.Entities.Course;
import Core.Entities.Student;

import Core.Interfaces.ICourseDAO;
import Core.Interfaces.IStudentDAO;

import Utilities.FileIO.CourseFileIO;
import Utilities.FileIO.IFileIO;
import Utilities.Validation.CourseValidation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDAO extends BaseDAO<Course> implements ICourseDAO {

    private static final String DEFAULT_PATH = "src/DataObject/data/Courses.txt";

    /** Phải tạo StudentDAO trước: CourseFileIO cần Map studentId -> Student để parse. */
    public CourseDAO(IStudentDAO studentDAO) {
        this(new CourseFileIO(DEFAULT_PATH, toMap(studentDAO)));
    }

    public CourseDAO(IFileIO<Course> fileIO) {
        super(fileIO, "course");
    }

    private static Map<String, Student> toMap(IStudentDAO dao) {
        Map<String, Student> map = new HashMap<>();
        for (Student s : dao.readAll()) map.put(s.getId(), s);
        return map;
    }

    @Override
    protected String keyOf(Course c) { return c.getCourseId(); }

    @Override
    public List<Course> findByName(String name) {
        List<Course> result = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) return result;
        String kw = name.trim().toLowerCase();
        for (Course c : items) {
            if (c.getCourseName() != null && c.getCourseName().toLowerCase().contains(kw)) {
                result.add(c);
            }
        }
        return result;
    }

    @Override
    public List<Course> findByStudentId(String studentId) {
        List<Course> result = new ArrayList<>();
        if (studentId == null) return result;
        String id = studentId.trim();
        for (Course c : items) {
            if (c.getStudent().getId().equalsIgnoreCase(id)) result.add(c);
        }
        return result;
    }

    /**
     * FIND
     * */
    @Override
    public List<Course> findByStartDate(String input) {
        List<Course> result = new ArrayList<>();
        LocalDate target = CourseValidation.parseLocalDate(input);
        if (target == null) return result;
        for (Course c : items) {
            LocalDate d = c.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (d.equals(target)) result.add(c);
        }
        return result;
    }
}
