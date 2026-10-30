package businessObject;

import core.entities.Student;
import Doubles.Fake.FakeCourseDAO;
import Doubles.Fake.FakeStudentDAO;
import Support.CsvLogger;
import utilities.validation.CourseValidation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * STATE TEST cho CourseList (dùng FAKE DAO): kiểm tra KẾT QUẢ cuối cùng.
 * (Kiểm tra cách gọi DAO -> CourseListInteractionTest.)
 */
public class CourseListStateTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    private FakeStudentDAO studentDAO;
    private FakeCourseDAO courseDAO;
    private CourseList courseList;
    private Date future;

    @Before
    public void setUp() {
        studentDAO = new FakeStudentDAO();
        courseDAO = new FakeCourseDAO();
        courseList = new CourseList(courseDAO, studentDAO);
        studentDAO.add(new Student("STU0001", "Nguyen Van A", "SE", 3.5));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        future = cal.getTime();
    }

    @Test
    public void add_validCourse_setsDirty() {
        courseList.add("PRJ301", "STU0001", "Java Web", 6, future);
        assertTrue(courseList.isDirty());
        assertEquals(1, courseDAO.readAll().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_unknownStudent_throws() {
        courseList.add("PRJ301", "STU9999", "Java Web", 6, future);
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_durationTooShort_throws() {
        courseList.add("PRJ301", "STU0001", "Java Web", CourseValidation.MIN_DURATION - 1, future);
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_pastDate_throws() {
        courseList.add("PRJ301", "STU0001", "Java Web", 6, new Date(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_duplicateCourseId_throws() {
        courseList.add("PRJ301", "STU0001", "Java Web", 6, future);
        courseList.add("PRJ301", "STU0001", "Java Web", 6, future);
    }

    @Test
    public void totalDuration_sumsAllCoursesOfStudent() {
        courseList.add("PRJ301", "STU0001", "Java Web", 6, future);
        courseList.add("DBI202", "STU0001", "Database", 5, future);
        assertEquals(11, courseList.totalDuration("STU0001"));
    }

    @Test
    public void save_resetsDirty() {
        courseList.add("PRJ301", "STU0001", "Java Web", 6, future);
        assertTrue(courseList.save());
        assertFalse(courseList.isDirty());
    }
}
