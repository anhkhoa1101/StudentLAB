package Core.Interfaces;

import Core.Entities.Course;
import java.util.List;

public interface ICourseDAO extends IBaseDAO<Course> {
    List<Course> findByName(String name);
    List<Course> findByStudentId(String studentId);
    List<Course> findByStartDate(String input);
}