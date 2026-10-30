package presentation.menu;

import businessObject.StudentList;
import core.entities.Course;
import core.entities.Student;
import Doubles.Fake.FakeCourseDAO;
import Doubles.Fake.FakeStudentDAO;
import Support.ConsoleSim;
import Support.CsvLogger;
import Support.TC;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test Case nhóm Student (TC-S01 .. TC-S20). Tên method theo mẫu "[Mã TC]_Function".
 *
 *  - StudentMenu được test cùng StudentList thật, DAO là FakeStudentDAO/FakeCourseDAO (RAM).
 *  - ConsoleSim "gõ phím" thay người dùng và bắt lại chữ in ra màn hình.
 *  - Dữ liệu mẫu giống Students.txt / Courses.txt gốc, nạp lại trước MỖI test.
 *  - CsvLogger ghi kết quả từng TC vào test-results/StudentMenuTest_Log.csv.
 */
public class StudentMenuTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    private static Locale oldLocale;

    private FakeStudentDAO studentDAO;
    private FakeCourseDAO courseDAO;
    private StudentMenu menu;

    @BeforeClass
    public static void forceUsLocale() {
        oldLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);       // Student.toString dùng %.1f: máy locale vi sẽ in "3,4" thay vì "3.4"
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

        // STU0006 và STU0007 chưa có khóa học (như dữ liệu gốc)
        Date d = new Date();
        courseDAO.add(new Course("CSE201", s2, "Database Systems", 10, d));
        courseDAO.add(new Course("MAT301", s3, "Discrete Mathematics", 8, d));
        courseDAO.add(new Course("AI401", s4, "Machine Learning", 10, d));
        courseDAO.add(new Course("DS501", s5, "Big Data Analytics", 8, d));
        courseDAO.add(new Course("PRJ301", s1, "Java Web Project", 6, d));
        courseDAO.add(new Course("PRO192", s3, "Java Programming", 8, d));
        courseDAO.add(new Course("DBI202", s2, "Database Design", 5, d));

        menu = new StudentMenu(new StudentList(studentDAO, courseDAO));
    }

    // ================================================================ helpers

    /** Gõ các dòng input rồi chạy 1 chức năng; bắt IllegalArgumentException giống Program.main. */
    private String run(Runnable action, String... input) {
        ConsoleSim.feed(input);
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return ConsoleSim.output();
    }

    /** Các dòng dữ liệu sinh viên trong màn hình (dòng bắt đầu bằng "STU"), tách thành các cột. */
    private static List<String[]> dataRows(String out) {
        List<String[]> rows = new ArrayList<>();
        for (String line : ConsoleSim.lines(out)) {
            if (line.startsWith("STU")) {
                String[] cells = line.split("\\|");
                for (int i = 0; i < cells.length; i++) cells[i] = cells[i].trim();
                rows.add(cells);
            }
        }
        return rows;
    }

    private static List<String> ids(String out) {
        List<String> ids = new ArrayList<>();
        for (String[] r : dataRows(out)) ids.add(r[0]);
        return ids;
    }

    private Student stored(String id) {
        return studentDAO.findByID(id).orElse(null);
    }

    private static void assertShows(String out, String text) {
        assertTrue("Màn hình phải có: \"" + text + "\"", out.contains(text));
    }

    // ================================================================ 2. Add student

    @Test
    @TC(id = "TC-S01", function = "2. Add student", input = "ID = STU12",
        expected = "In “Invalid ID. Format: STU0000.” và hỏi lại ID")
    public void TC_S01_AddStudent_idTooShort_isRejectedAndReprompted() {
        String out = run(menu::add, "STU12", "STU0008", "Tran Van C", "Software Engineering", "3.5");
        assertShows(out, "Invalid ID. Format: STU0000.");
        assertEquals("phải hỏi lại ID", 2, ConsoleSim.count(out, "ID (STU0000): "));
    }

    @Test
    @TC(id = "TC-S02", function = "2. Add student", input = "ID = abc1234",
        expected = "Như TC-S01 (sai mẫu STU + 4 chữ số)")
    public void TC_S02_AddStudent_idWrongPattern_isRejectedAndReprompted() {
        String out = run(menu::add, "abc1234", "STU0008", "Tran Van C", "Software Engineering", "3.5");
        assertShows(out, "Invalid ID. Format: STU0000.");
        assertEquals(2, ConsoleSim.count(out, "ID (STU0000): "));
    }

    @Test
    @TC(id = "TC-S03", function = "2. Add student",
        input = "ID = stu0008 (chữ thường), Name = Tran Van C, Major = Software Engineering, GPA = 3.5",
        expected = "Được chấp nhận; sinh viên lưu với ID STU0008 (chữ hoa); in “Added successfully (not saved to file yet).”")
    public void TC_S03_AddStudent_lowercaseId_isStoredUppercase() {
        String out = run(menu::add, "stu0008", "Tran Van C", "Software Engineering", "3.5");
        assertShows(out, "Added successfully (not saved to file yet).");
        Student s = stored("STU0008");
        assertTrue("sinh viên phải được thêm", s != null);
        assertEquals("STU0008", s.getId());
        assertEquals("Tran Van C", s.getName());
        assertEquals("Software Engineering", s.getMajor());
        assertEquals(3.5, s.getGpa(), 0.0001);
        assertEquals(8, studentDAO.readAll().size());
    }

    @Test
    @TC(id = "TC-S04", function = "2. Add student", input = "ID = STU0001 (đã tồn tại), các trường khác hợp lệ",
        expected = "In “Error: Student ID already exists: STU0001”; danh sách không đổi")
    public void TC_S04_AddStudent_duplicateId_isRejected() {
        String out = run(menu::add, "STU0001", "Tran Van C", "Software Engineering", "3.5");
        assertShows(out, "Error: Student ID already exists: STU0001");
        assertEquals("danh sách không đổi", 7, studentDAO.readAll().size());
        assertEquals("dữ liệu cũ không bị ghi đè", "Nguyen Van A", stored("STU0001").getName());
    }

    @Test
    @TC(id = "TC-S05", function = "2. Add student", input = "Name = Nguyen123",
        expected = "In “Invalid name (letters and spaces only).” và hỏi lại")
    public void TC_S05_AddStudent_nameWithDigits_isRejectedAndReprompted() {
        String out = run(menu::add, "STU0008", "Nguyen123", "Nguyen Van C", "Software Engineering", "3.5");
        assertShows(out, "Invalid name (letters and spaces only).");
        assertEquals("phải hỏi lại Name", 2, ConsoleSim.count(out, "Name: "));
    }

    @Test
    @TC(id = "TC-S06", function = "2. Add student", input = "Name = A (1 ký tự)",
        expected = "Như TC-S05 (tên tối thiểu 2 ký tự)")
    public void TC_S06_AddStudent_nameTooShort_isRejectedAndReprompted() {
        String out = run(menu::add, "STU0008", "A", "Nguyen Van C", "Software Engineering", "3.5");
        assertShows(out, "Invalid name (letters and spaces only).");
        assertEquals(2, ConsoleSim.count(out, "Name: "));
    }

    @Test
    @TC(id = "TC-S07", function = "2. Add student", input = "Major để trống (Enter)",
        expected = "In “Must not be empty.” và hỏi lại")
    public void TC_S07_AddStudent_emptyMajor_isRejectedAndReprompted() {
        String out = run(menu::add, "STU0008", "Tran Van C", "", "Software Engineering", "3.5");
        assertShows(out, "Must not be empty.");
        assertEquals("phải hỏi lại Major", 2, ConsoleSim.count(out, "Major: "));
    }

    @Test
    @TC(id = "TC-S08", function = "2. Add student", input = "GPA = 4.5",
        expected = "In “Enter a number from 0.0 to 4.0.” và hỏi lại")
    public void TC_S08_AddStudent_gpaTooHigh_isRejectedAndReprompted() {
        String out = run(menu::add, "STU0008", "Tran Van C", "Software Engineering", "4.5", "3.5");
        assertShows(out, "Enter a number from 0.0 to 4.0.");
        assertEquals("phải hỏi lại GPA", 2, ConsoleSim.count(out, "GPA (0.0 - 4.0): "));
    }

    @Test
    @TC(id = "TC-S09", function = "2. Add student", input = "GPA = -1, sau đó abc",
        expected = "Mỗi lần đều in “Enter a number from 0.0 to 4.0.”")
    public void TC_S09_AddStudent_gpaNegativeThenText_bothRejected() {
        String out = run(menu::add, "STU0008", "Tran Van C", "Software Engineering", "-1", "abc", "3.5");
        assertEquals("2 lần nhập sai -> 2 thông báo lỗi", 2,
                ConsoleSim.count(out, "Enter a number from 0.0 to 4.0."));
        assertEquals(3, ConsoleSim.count(out, "GPA (0.0 - 4.0): "));
    }

    @Test
    @TC(id = "TC-S10", function = "2. Add student", input = "GPA = 3,5 (dấu phẩy)",
        expected = "Được chấp nhận và hiểu là 3.5")
    public void TC_S10_AddStudent_gpaWithComma_isAcceptedAs3Point5() {
        String out = run(menu::add, "STU0008", "Tran Van C", "Software Engineering", "3,5");
        assertFalse(out.contains("Enter a number from"));
        assertShows(out, "Added successfully");
        assertEquals(3.5, stored("STU0008").getGpa(), 0.0001);
    }

    // ================================================================ 3. Search by ID

    @Test
    @TC(id = "TC-S11", function = "3. Search by ID", input = "STU0003",
        expected = "Hiển thị 1 dòng: STU0003 | Nguyen Van An | Software Engineering | 3.4")
    public void TC_S11_SearchById_existingId_showsOneRow() {
        String out = run(menu::searchById, "STU0003");
        List<String[]> rows = dataRows(out);
        assertEquals("đúng 1 dòng kết quả", 1, rows.size());
        assertEquals("STU0003", rows.get(0)[0]);
        assertEquals("Nguyen Van An", rows.get(0)[1]);
        assertEquals("Software Engineering", rows.get(0)[2]);
        assertEquals("3.4", rows.get(0)[3]);
    }

    @Test
    @TC(id = "TC-S12", function = "3. Search by ID", input = "STU0099",
        expected = "In “Student not found.”")
    public void TC_S12_SearchById_unknownId_showsNotFound() {
        String out = run(menu::searchById, "STU0099");
        assertShows(out, "Student not found.");
        assertTrue("không in dòng sinh viên nào", dataRows(out).isEmpty());
    }

    // ================================================================ 4. Update

    @Test
    @TC(id = "TC-S13", function = "4. Update", input = "ID = STU0001, nhấn Enter ở cả 3 trường",
        expected = "Dữ liệu giữ nguyên; in “Updated successfully (not saved to file yet).”")
    public void TC_S13_Update_pressEnterEverywhere_keepsData() {
        String out = run(menu::update, "STU0001", "", "", "");
        assertShows(out, "Updated successfully (not saved to file yet).");
        Student s = stored("STU0001");
        assertEquals("Nguyen Van A", s.getName());
        assertEquals("Software Engineering", s.getMajor());
        assertEquals(3.4, s.getGpa(), 0.0001);
    }

    @Test
    @TC(id = "TC-S14", function = "4. Update", input = "ID = STU0001, GPA = 5",
        expected = "In “Enter a number from 0.0 to 4.0.” và hỏi lại")
    public void TC_S14_Update_gpaTooHigh_isRejectedAndReprompted() {
        String out = run(menu::update, "STU0001", "", "", "5", "");
        assertShows(out, "Enter a number from 0.0 to 4.0.");
        assertEquals("phải hỏi lại GPA", 2, ConsoleSim.count(out, "New GPA: "));
        assertEquals("GPA cũ được giữ", 3.4, stored("STU0001").getGpa(), 0.0001);
    }

    // ================================================================ 5. List by major

    @Test
    @TC(id = "TC-S15", function = "5. List by major", input = "software (chữ thường)",
        expected = "Liệt kê STU0001 và STU0003 (thêm STU0008 nếu đã chạy TC-S03); không phân biệt hoa thường")
    public void TC_S15_ListByMajor_isCaseInsensitive() {
        String out = run(menu::listByMajor, "software");
        List<String> ids = ids(out);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("STU0001"));
        assertTrue(ids.contains("STU0003"));
    }

    // ================================================================ 10. Sort by GPA

    @Test
    @TC(id = "TC-S16", function = "10. Sort by GPA", input = "— (dữ liệu gốc)",
        expected = "Thứ tự GPA tăng dần: STU0005 (3.2), STU0001 (3.4), STU0003 (3.4), STU0007 (3.5), STU0004 (3.7), STU0002 (3.8), STU0006 (3.9)")
    public void TC_S16_SortByGpa_isAscending() {
        String out = run(menu::sortByGpa);
        List<String> expected = new ArrayList<>();
        for (String id : new String[]{"STU0005", "STU0001", "STU0003", "STU0007", "STU0004", "STU0002", "STU0006"})
            expected.add(id);
        assertEquals(expected, ids(out));
    }

    // ================================================================ 9. Remove

    @Test
    @TC(id = "TC-S17", function = "9. Remove", input = "ID = STU0002 (đang có khóa học), xác nhận Y",
        expected = "In “Error: Cannot delete: student is enrolled in course(s).”; sinh viên vẫn còn trong danh sách")
    public void TC_S17_Remove_enrolledStudent_isBlocked() {
        String out = run(menu::remove, "STU0002", "Y");
        assertShows(out, "Error: Cannot delete: student is enrolled in course(s).");
        assertTrue("STU0002 vẫn còn", stored("STU0002") != null);
        assertEquals(7, studentDAO.readAll().size());
    }

    @Test
    @TC(id = "TC-S18", function = "9. Remove", input = "ID = STU0006 (chưa có khóa học), xác nhận Y",
        expected = "In “Removed successfully (not saved to file yet).”; chức năng 1 không còn STU0006")
    public void TC_S18_Remove_studentWithoutCourse_isRemoved() {
        String afterRemove = run(menu::remove, "STU0006", "Y");
        assertShows(afterRemove, "Removed successfully (not saved to file yet).");

        String all = run(menu::listAll);                       // chức năng 1
        List<String> ids = ids(all.substring(afterRemove.length()));
        assertFalse("STU0006 không còn trong danh sách", ids.contains("STU0006"));
        assertEquals(6, ids.size());
    }

    @Test
    @TC(id = "TC-S19", function = "9. Remove", input = "ID = STU0007, xác nhận N",
        expected = "Không xoá, quay lại menu")
    public void TC_S19_Remove_confirmNo_keepsStudent() {
        String out = run(menu::remove, "STU0007", "N");
        assertFalse(out.contains("Removed successfully"));
        assertFalse(out.contains("Error"));
        assertTrue("STU0007 vẫn còn", stored("STU0007") != null);
        assertEquals(7, studentDAO.readAll().size());
    }

    @Test
    @TC(id = "TC-S20", function = "9. Remove", input = "ID = STU0099, xác nhận Y",
        expected = "In “Error: Student not found: STU0099”")
    public void TC_S20_Remove_unknownId_showsError() {
        String out = run(menu::remove, "STU0099", "Y");
        assertShows(out, "Error: Student not found: STU0099");
        assertEquals(7, studentDAO.readAll().size());
    }
}
