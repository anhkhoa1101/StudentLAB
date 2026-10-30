package presentation.menu;

import businessObject.CourseList;
import core.entities.Course;
import core.entities.Student;
import Doubles.Fake.FakeCourseDAO;
import Doubles.Fake.FakeStudentDAO;
import Support.ConsoleSim;
import Support.CsvLogger;
import Support.TC;
import utilities.validation.CourseValidation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test Case nhóm Course (TC-C01 .. TC-C13). Tên method theo mẫu "[Mã TC]_Function".
 * CourseMenu + CourseList thật, DAO là Fake (RAM). Dữ liệu mẫu = 7 khóa học hợp lệ của Courses.txt gốc.
 */
public class CourseMenuTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    private static Locale oldLocale;

    private FakeStudentDAO studentDAO;
    private FakeCourseDAO courseDAO;
    private CourseMenu menu;

    @BeforeClass
    public static void forceUsLocale() {
        oldLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterClass
    public static void restoreLocale() {
        Locale.setDefault(oldLocale);
    }

    @Before
    public void setUp() {
        studentDAO = new FakeStudentDAO();
        courseDAO = new FakeCourseDAO();

        Student s1 = new Student("STU0001", "Nguyen Van A", "Software Engineering", 3.4);
        Student s2 = new Student("STU0002", "Tran Thi B", "Information Systems", 3.8);
        Student s3 = new Student("STU0003", "Nguyen Van An", "Software Engineering", 3.4);
        Student s4 = new Student("STU0004", "Tran Thi Bich", "Information Systems", 3.7);
        Student s5 = new Student("STU0005", "Le Hoang Minh", "Computer Science", 3.2);
        Student s6 = new Student("STU0006", "Pham Quoc Huy", "Artificial Intelligence", 3.9);
        Student s7 = new Student("STU0007", "Vo Thanh Tam", "Data Science", 3.5);
        for (Student s : new Student[]{s1, s2, s3, s4, s5, s6, s7}) studentDAO.add(s);

        // 7 khóa học hợp lệ của Courses.txt gốc (STU0006, STU0007 chưa có khóa học)
        courseDAO.add(new Course("CSE201", s2, "Database Systems", 10, CourseValidation.parseDate("15/09/2025")));
        courseDAO.add(new Course("MAT301", s3, "Discrete Mathematics", 8, CourseValidation.parseDate("20/09/2025")));
        courseDAO.add(new Course("AI401", s4, "Machine Learning", 10, CourseValidation.parseDate("05/10/2025")));
        courseDAO.add(new Course("DS501", s5, "Big Data Analytics", 8, CourseValidation.parseDate("10/10/2025")));
        courseDAO.add(new Course("PRJ301", s1, "Java Web Project", 6, CourseValidation.parseDate("20/10/2025")));
        courseDAO.add(new Course("PRO192", s3, "Java Programming", 8, CourseValidation.parseDate("25/10/2025")));
        courseDAO.add(new Course("DBI202", s2, "Database Design", 5, CourseValidation.parseDate("01/11/2025")));

        menu = new CourseMenu(new CourseList(courseDAO, studentDAO));
    }

    // ================================================================ helpers

    private String run(Runnable action, String... input) {
        ConsoleSim.feed(input);
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return ConsoleSim.output();
    }

    private static void assertShows(String out, String text) {
        assertTrue("Màn hình phải có: \"" + text + "\"", out.contains(text));
    }

    /** Chuyển màn hình chức năng 7 thành danh sách: "== STU0002 - Tên ==" rồi "STU0002:CSE201" ... */
    private static List<String> groupedLines(String out) {
        List<String> r = new ArrayList<>();
        String current = "";
        for (String line : ConsoleSim.lines(out)) {
            if (line.startsWith("== ")) {
                current = line.substring(3, line.indexOf(" - "));
                r.add(line);
            } else if (line.contains("|") && !line.startsWith("CourseID")) {
                r.add(current + ":" + line.split("\\|")[0].trim());
            }
        }
        return r;
    }

    // ================================================================ 6. Add course

    @Test
    @TC(id = "TC-C01", function = "6. Add course",
        input = "Student ID = STU0099 (đúng mẫu nhưng không tồn tại), các trường khác hợp lệ",
        expected = "Sau khi nhập hết các trường: “Error: Student ID does not exist!”")
    public void TC_C01_AddCourse_unknownStudent_showsErrorAfterAllFields() {
        String out = run(menu::add, "CSE301", "STU0099", "Operating Systems", "8", "15/12/2027");
        assertShows(out, "Error: Student ID does not exist!");
        assertTrue("phải hỏi đủ các trường rồi mới báo lỗi",
                out.indexOf("Start date") < out.indexOf("Error: Student ID does not exist!"));
        assertFalse(out.contains("Added successfully"));
        assertFalse(courseDAO.findByID("CSE301").isPresent());
    }

    @Test
    @TC(id = "TC-C02", function = "6. Add course", input = "Duration = 4, sau đó 11",
        expected = "Mỗi lần in “Enter an integer from 5 to 10.” và hỏi lại")
    public void TC_C02_AddCourse_durationOutOfRange_bothRejected() {
        String out = run(menu::add, "CSE301", "STU0001", "Operating Systems", "4", "11", "8", "15/12/2027");
        assertEquals("2 lần nhập sai -> 2 thông báo lỗi", 2,
                ConsoleSim.count(out, "Enter an integer from 5 to 10."));
        assertEquals("hỏi Duration 3 lần", 3, ConsoleSim.count(out, "Duration (5-10 weeks): "));
    }

    @Test
    @TC(id = "TC-C03", function = "6. Add course", input = "Start date = 01/01/2020 (quá khứ)",
        expected = "In “Invalid date or not a future date. Format: dd/MM/yyyy.” và hỏi lại")
    public void TC_C03_AddCourse_pastDate_isRejectedAndReprompted() {
        String out = run(menu::add, "CSE301", "STU0001", "Operating Systems", "8", "01/01/2020", "15/12/2027");
        assertShows(out, "Invalid date or not a future date. Format: dd/MM/yyyy.");
        assertEquals(2, ConsoleSim.count(out, "Start date (dd/MM/yyyy, must be in the future): "));
    }

    @Test
    @TC(id = "TC-C04", function = "6. Add course", input = "Start date = 31/02/2027 (ngày không tồn tại)",
        expected = "Như TC-C03")
    public void TC_C04_AddCourse_nonexistentDate_isRejectedAndReprompted() {
        String out = run(menu::add, "CSE301", "STU0001", "Operating Systems", "8", "31/02/2027", "15/12/2027");
        assertShows(out, "Invalid date or not a future date. Format: dd/MM/yyyy.");
        assertEquals(2, ConsoleSim.count(out, "Start date (dd/MM/yyyy, must be in the future): "));
    }

    @Test
    @TC(id = "TC-C05", function = "6. Add course", input = "Start date = Sep 9, 2027",
        expected = "Được chấp nhận; chức năng 7 hiển thị 09/09/2027")
    public void TC_C05_AddCourse_textMonthDate_isAcceptedAndShownAsDdMmYyyy() {
        String out = run(menu::add, "CSE301", "STU0001", "Operating Systems", "8", "Sep 9, 2027");
        assertShows(out, "Added successfully");
        assertFalse(out.contains("Invalid date"));

        String list = run(menu::listGrouped).substring(out.length());     // chức năng 7
        assertShows(list, "09/09/2027");
    }

    @Test
    @TC(id = "TC-C06", function = "6. Add course", input = "CSE301, STU0001, Operating Systems, 8, 15/12/2027",
        expected = "In “Added successfully (not saved to file yet).”; chức năng 7 có khóa học mới ở nhóm STU0001")
    public void TC_C06_AddCourse_validData_appearsInStudentGroup() {
        String out = run(menu::add, "CSE301", "STU0001", "Operating Systems", "8", "15/12/2027");
        assertShows(out, "Added successfully (not saved to file yet).");

        String list = run(menu::listGrouped).substring(out.length());     // chức năng 7
        List<String> lines = groupedLines(list);
        assertTrue("nhóm STU0001 có CSE301: " + lines, lines.contains("STU0001:CSE301"));
        assertTrue("STU0001 vẫn còn PRJ301", lines.contains("STU0001:PRJ301"));
    }

    @Test
    @TC(id = "TC-C07", function = "6. Add course", input = "Course ID = CSE201 (đã tồn tại), dữ liệu khác hợp lệ",
        expected = "In “Error: Course ID already exists!”")
    public void TC_C07_AddCourse_duplicateCourseId_isRejected() {
        String out = run(menu::add, "CSE201", "STU0001", "Operating Systems", "8", "15/12/2027");
        assertShows(out, "Error: Course ID already exists!");
        assertEquals("danh sách không đổi", 7, courseDAO.readAll().size());
        assertEquals("khóa học cũ không bị ghi đè", "Database Systems", courseDAO.findByID("CSE201").get().getCourseName());
    }

    @Test
    @TC(id = "TC-C08", function = "6. Add course", input = "Course ID để trống",
        expected = "In “Must not be empty.” và hỏi lại")
    public void TC_C08_AddCourse_emptyCourseId_isRejectedAndReprompted() {
        String out = run(menu::add, "", "CSE301", "STU0001", "Operating Systems", "8", "15/12/2027");
        assertShows(out, "Must not be empty.");
        assertEquals("phải hỏi lại Course ID", 2, ConsoleSim.count(out, "Course ID: "));
    }

    // ================================================================ 7. List grouped

    @Test
    @TC(id = "TC-C09", function = "7. List grouped", input = "— (dữ liệu gốc)",
        expected = "Nhóm theo STU0001..STU0005; mỗi nhóm có dòng “== STU000x - Tên ==”; trong nhóm sắp theo Course ID (STU0002: CSE201, DBI202; STU0003: MAT301, PRO192)")
    public void TC_C09_ListGrouped_groupsByStudentAndSortsByCourseId() {
        String out = run(menu::listGrouped);
        List<String> expected = Arrays.asList(
                "== STU0001 - Nguyen Van A ==", "STU0001:PRJ301",
                "== STU0002 - Tran Thi B ==", "STU0002:CSE201", "STU0002:DBI202",
                "== STU0003 - Nguyen Van An ==", "STU0003:MAT301", "STU0003:PRO192",
                "== STU0004 - Tran Thi Bich ==", "STU0004:AI401",
                "== STU0005 - Le Hoang Minh ==", "STU0005:DS501");
        assertEquals(expected, groupedLines(out));
    }

    // ================================================================ 8. Total duration

    @Test
    @TC(id = "TC-C10", function = "8. Total duration", input = "STU0003",
        expected = "“Total duration of STU0003: 16 week(s).” (MAT301 8 + PRO192 8)")
    public void TC_C10_TotalDuration_stu0003_is16() {
        assertShows(run(menu::totalDuration, "STU0003"), "Total duration of STU0003: 16 week(s).");
    }

    @Test
    @TC(id = "TC-C11", function = "8. Total duration", input = "STU0002",
        expected = "“Total duration of STU0002: 15 week(s).” (CSE201 10 + DBI202 5)")
    public void TC_C11_TotalDuration_stu0002_is15() {
        assertShows(run(menu::totalDuration, "STU0002"), "Total duration of STU0002: 15 week(s).");
    }

    @Test
    @TC(id = "TC-C12", function = "8. Total duration", input = "STU0007 (chưa có khóa học)",
        expected = "“Total duration of STU0007: 0 week(s).”")
    public void TC_C12_TotalDuration_studentWithoutCourse_isZero() {
        assertShows(run(menu::totalDuration, "STU0007"), "Total duration of STU0007: 0 week(s).");
    }

    @Test
    @TC(id = "TC-C13", function = "8. Total duration", input = "STU0099",
        expected = "In “Error: Student ID does not exist!”")
    public void TC_C13_TotalDuration_unknownStudent_showsError() {
        assertShows(run(menu::totalDuration, "STU0099"), "Error: Student ID does not exist!");
    }
}
