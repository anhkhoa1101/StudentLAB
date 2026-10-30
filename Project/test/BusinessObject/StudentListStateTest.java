package businessObject;

import core.entities.Course;
import core.entities.Student;
import Doubles.Fake.FakeCourseDAO;
import Doubles.Fake.FakeStudentDAO;
import Support.CsvLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * STATE TEST cho StudentList (dùng FAKE DAO): "kết quả cuối cùng có đúng không?"
 * (Còn "StudentList có gọi DAO đúng cách không?" -> StudentListInteractionTest.)
 */
public class StudentListStateTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    private FakeStudentDAO studentDAO;
    private FakeCourseDAO courseDAO;
    private StudentList studentList;

    @Before
    public void setUp() {
        studentDAO = new FakeStudentDAO();
        courseDAO = new FakeCourseDAO();
        studentList = new StudentList(studentDAO, courseDAO);
    }

    @Test
    public void add_validStudent_returnsTrueAndStored() {
        Student s = new Student("STU0001", "Nguyen Van A", "SE", 3.5);
        assertTrue(studentList.add(s));
        assertTrue(studentList.findById("STU0001").isPresent());
        assertTrue(studentDAO.saved);              // đã gọi save()
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_duplicateId_throws() {
        studentList.add(new Student("STU0001", "Nguyen Van A", "SE", 3.5));
        studentList.add(new Student("STU0001", "Tran Thi B", "IS", 3.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_invalidIdFormat_throws() {
        studentList.add(new Student("ABC123", "Nguyen Van A", "SE", 3.5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_gpaOutOfRange_throws() {
        studentList.add(new Student("STU0002", "Le Minh", "SE", 5.0));
    }

    @Test
    public void add_lowercaseId_isNormalizedToUpper() {
        studentList.add(new Student("stu0003", "Le Minh", "SE", 3.0));
        assertEquals("STU0003", studentList.getAll().get(0).getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void delete_studentWithCourse_throws() {
        Student s = new Student("STU0001", "Nguyen Van A", "SE", 3.5);
        studentList.add(s);
        courseDAO.add(new Course("PRJ301", s, "Java Web", 6, new Date()));
        studentList.delete("STU0001");
    }

    @Test
    public void delete_studentWithoutCourse_ok() {
        studentList.add(new Student("STU0001", "Nguyen Van A", "SE", 3.5));
        assertTrue(studentList.delete("STU0001"));
        assertFalse(studentList.findById("STU0001").isPresent());
    }

    @Test
    public void sortByGpaAsc_orderIsAscending() {
        studentList.add(new Student("STU0001", "An Binh", "SE", 3.9));
        studentList.add(new Student("STU0002", "Cao Dung", "SE", 2.1));
        studentList.add(new Student("STU0003", "Em Giang", "SE", 3.0));
        List<Student> r = studentList.sortByGpaAsc();
        assertEquals("STU0002", r.get(0).getId());
        assertEquals("STU0001", r.get(2).getId());
    }
}
