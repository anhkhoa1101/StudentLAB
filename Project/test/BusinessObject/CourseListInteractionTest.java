package businessObject;

import core.entities.Course;
import core.entities.Student;
import Doubles.Mock.MockCourseDAO;
import Doubles.Mock.MockStudentDAO;
import Support.CsvLogger;
import Support.TestLog;
import utilities.validation.CourseValidation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * INTERACTION TEST cho CourseList (dùng MOCK DAO): kiểm tra CourseList gọi StudentDAO / CourseDAO đúng cách.
 * (Xem giải thích khác biệt State / Interaction ở StudentListInteractionTest.)
 */
public class CourseListInteractionTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    private MockStudentDAO studentDAO;
    private MockCourseDAO courseDAO;
    private CourseList courseList;
    private Student s1;
    private Date future;

    @Before
    public void setUp() {
        studentDAO = new MockStudentDAO();
        courseDAO = new MockCourseDAO();
        courseList = new CourseList(courseDAO, studentDAO);

        s1 = new Student("STU0001", "Nguyen Van A", "SE", 3.5);
        studentDAO.findStub.put("STU0001", s1);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        future = cal.getTime();
    }

    private Course course(String id, Student s, int duration) {
        TestLog.input("Course(" + id + ", " + s.getId() + ", duration=" + duration + ")");
        return new Course(id, s, "Course " + id, duration, future);
    }

    // ================================================================ add

    @Test
    public void add_valid_buildsCourseFromLookedUpStudent_andMarksDirtyWithoutSaving() {
        courseList.add("  PRJ301 ", "STU0001", " Java Web ", 6, future);

        assertEquals(Arrays.asList("STU0001"), studentDAO.findCalls);
        assertEquals(1, courseDAO.addCalls.size());
        Course c = courseDAO.addCalls.get(0);
        assertEquals("PRJ301", c.getCourseId());
        assertEquals("Java Web", c.getCourseName());
        assertSame("phải dùng đúng đối tượng Student do StudentDAO trả về", s1, c.getStudent());
        assertEquals(6, c.getDuration());
        assertSame(future, c.getStartDate());
        assertTrue(courseList.isDirty());
        assertEquals("add chưa được phép save", 0, courseDAO.saveCalls);
    }

    @Test
    public void add_emptyCourseId_failsBeforeLookingUpStudent() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> courseList.add("   ", "STU0001", "Java Web", 6, future));

        assertEquals("Course ID cannot be empty!", e.getMessage());
        assertTrue(studentDAO.findCalls.isEmpty());
        assertTrue(courseDAO.addCalls.isEmpty());
        assertFalse(courseList.isDirty());
    }

    @Test
    public void add_unknownStudent_throwsAndDoesNotAdd() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> courseList.add("PRJ301", "STU9999", "Java Web", 6, future));

        assertEquals("Student ID does not exist!", e.getMessage());
        assertEquals(Arrays.asList("STU9999"), studentDAO.findCalls);
        assertTrue(courseDAO.addCalls.isEmpty());
        assertFalse(courseList.isDirty());
    }

    @Test
    public void add_emptyCourseName_throwsAndDoesNotAdd() {
        assertThrows(IllegalArgumentException.class,
                () -> courseList.add("PRJ301", "STU0001", "  ", 6, future));
        assertTrue(courseDAO.addCalls.isEmpty());
    }

    @Test
    public void add_durationOutOfRange_throwsAndDoesNotAdd() {
        assertThrows(IllegalArgumentException.class,
                () -> courseList.add("PRJ301", "STU0001", "Java Web", CourseValidation.MIN_DURATION - 1, future));
        assertThrows(IllegalArgumentException.class,
                () -> courseList.add("PRJ301", "STU0001", "Java Web", CourseValidation.MAX_DURATION + 1, future));
        assertTrue(courseDAO.addCalls.isEmpty());
    }

    @Test
    public void add_pastDate_throwsAndDoesNotAdd() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> courseList.add("PRJ301", "STU0001", "Java Web", 6, new Date(0)));

        assertEquals("Start date must be a future date!", e.getMessage());
        assertTrue(courseDAO.addCalls.isEmpty());
        assertFalse(courseList.isDirty());
    }

    @Test
    public void add_whenDaoRejectsDuplicate_throwsAndStaysClean() {
        courseDAO.addResult = false;

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> courseList.add("PRJ301", "STU0001", "Java Web", 6, future));

        assertEquals("Course ID already exists!", e.getMessage());
        assertEquals("vẫn đã thử add", 1, courseDAO.addCalls.size());
        assertFalse("add thất bại thì không được đánh dấu dirty", courseList.isDirty());
    }

    // ================================================================ totalDuration

    @Test
    public void totalDuration_queriesCoursesByCanonicalIdFromStudentEntity() {
        studentDAO.findStub.put("stu0001", s1);               // người dùng gõ chữ thường
        courseDAO.findByStudentIdStub = Arrays.asList(course("PRJ301", s1, 6), course("DBI202", s1, 5));

        assertEquals(11, courseList.totalDuration("stu0001"));

        assertEquals("phải truy vấn bằng ID chuẩn của sinh viên, không phải chuỗi người dùng gõ",
                Arrays.asList("STU0001"), courseDAO.findByStudentIdCalls);
    }

    @Test
    public void totalDuration_studentWithoutCourses_isZero() {
        assertEquals(0, courseList.totalDuration("STU0001"));
    }

    @Test
    public void totalDuration_unknownStudent_throwsWithoutQueryingCourses() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> courseList.totalDuration("STU9999"));

        assertEquals("Student ID does not exist!", e.getMessage());
        assertTrue(courseDAO.findByStudentIdCalls.isEmpty());
    }

    // ================================================================ truy vấn

    @Test
    public void listGroupedByStudent_sortsByStudentIdThenCourseId() {
        Student s2 = new Student("STU0002", "Tran Thi B", "IS", 3.0);
        courseDAO.readAllStub = Arrays.asList(
                course("B", s2, 5), course("Z", s1, 5), course("A", s1, 5), course("A", s2, 5));

        List<String> order = new ArrayList<>();
        for (Course c : courseList.listGroupedByStudent())
            order.add(c.getStudent().getId() + ":" + c.getCourseId());

        assertEquals(Arrays.asList("STU0001:A", "STU0001:Z", "STU0002:A", "STU0002:B"), order);
    }

    @Test
    public void courseCountByStudent_countsPerStudentSortedById() {
        Student s2 = new Student("STU0002", "Tran Thi B", "IS", 3.0);
        courseDAO.readAllStub = Arrays.asList(
                course("A", s2, 5), course("B", s1, 5), course("C", s1, 5), course("D", s1, 5));

        Map<String, Long> r = courseList.courseCountByStudent();

        assertEquals(Arrays.asList("STU0001", "STU0002"), new ArrayList<>(r.keySet()));
        assertEquals(Long.valueOf(3), r.get("STU0001"));
        assertEquals(Long.valueOf(1), r.get("STU0002"));
    }

    @Test
    public void courseCountByStudent_noCourses_returnsEmptyMap() {
        assertTrue(courseList.courseCountByStudent().isEmpty());
    }

    // ================================================================ save

    @Test
    public void save_success_resetsDirty() {
        courseList.add("PRJ301", "STU0001", "Java Web", 6, future);

        assertTrue(courseList.save());

        assertEquals(1, courseDAO.saveCalls);
        assertFalse(courseList.isDirty());
    }

    @Test
    public void save_failure_keepsDirtyFlag() {
        courseList.add("PRJ301", "STU0001", "Java Web", 6, future);
        courseDAO.saveResult = false;

        assertFalse(courseList.save());

        assertTrue("lưu thất bại thì vẫn còn thay đổi chưa lưu", courseList.isDirty());
    }
}
