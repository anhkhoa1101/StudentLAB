package presentation;

import Support.ConsoleSim;
import Support.CsvLogger;
import Support.DataFiles;
import Support.TC;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test Case nhóm File / Menu (TC-F01..F06, TC-M01..M02). Tên method theo mẫu "[Mã TC]_Function".
 *
 * Khác các test menu khác: test này chạy Program.main THẬT (DAO thật + file thật), nên dùng
 * DataFiles để ghi dữ liệu gốc trước mỗi test và khôi phục file của bạn sau mỗi test.
 * Phải chạy từ thư mục gốc dự án (NetBeans mặc định đã vậy) để đường dẫn src/DataObject/data/... đúng.
 */
public class ProgramTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    @Rule
    public final DataFiles dataFiles = new DataFiles();

    private static final Pattern STUDENT_ROW = Pattern.compile("^STU\\d{4}\\s+\\|.*");
    private static final Pattern COURSE_ROW = Pattern.compile("^\\S+\\s+\\| STU\\d{4}\\s+\\|.*");

    // ================================================================ helpers

    private String runMain(String... input) {
        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.US);
        try {
            ConsoleSim.feed(input);
            Program.main(new String[0]);
            return ConsoleSim.output();
        } finally {
            Locale.setDefault(old);
        }
    }

    private static int countRows(String out, Pattern p) {
        int n = 0;
        for (String line : ConsoleSim.lines(out)) if (p.matcher(line).matches()) n++;
        return n;
    }

    private static void assertShows(String out, String text) {
        assertTrue("Màn hình phải có: \"" + text + "\"", out.contains(text));
    }

    private static final String[] ADD_CSE301 =
            {"6", "CSE301", "STU0001", "Operating Systems", "8", "15/12/2027"};

    private static String[] concat(String[] a, String... b) {
        String[] r = new String[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    // ================================================================ Khởi động

    @Test
    @TC(id = "TC-F01", function = "Khởi động", input = "Courses.txt gốc (có dòng lỗi và dòng trùng)",
        expected = "In Skip line 3 / 7 / 10 và “Skip duplicate course ID: CSE201”; chương trình vẫn chạy, nạp 7 khóa học")
    public void TC_F01_Startup_badCoursesFile_skipsBadLinesAndLoads7() {
        String out = runMain("7", "12");

        int l3 = out.indexOf("Skip line 3 (expected 5 fields, got 3): ");
        int l7 = out.indexOf("Skip line 7 (expected 5 fields, got 3): ");
        int l10 = out.indexOf("Skip line 10 (expected 5 fields, got 1): ");
        int dup = out.indexOf("Skip duplicate course ID: CSE201");
        assertTrue("thiếu Skip line 3", l3 >= 0);
        assertTrue("thiếu Skip line 7", l7 >= 0);
        assertTrue("thiếu Skip line 10", l10 >= 0);
        assertTrue("thiếu Skip duplicate course ID: CSE201", dup >= 0);
        assertTrue("thứ tự thông báo: dòng 3, 7, 10 rồi dòng trùng", l3 < l7 && l7 < l10 && l10 < dup);

        assertEquals("nạp 7 khóa học", 7, countRows(out, COURSE_ROW));
        assertShows(out, "Goodbye!");
    }

    @Test
    @TC(id = "TC-F02", function = "Khởi động",
        input = "Thêm vào cuối Students.txt dòng STU0008, Le Van D, Data Science, abc",
        expected = "In “Skip line 8 (For input string: \"abc\"): …”; 7 sinh viên còn lại vẫn được nạp")
    public void TC_F02_Startup_badStudentGpa_skipsLineAndLoads7() throws IOException {
        DataFiles.write(DataFiles.STUDENTS,
                DataFiles.ORIGINAL_STUDENTS + "STU0008, Le Van D, Data Science, abc\n");

        String out = runMain("1", "12");

        assertShows(out, "Skip line 8 (For input string: \"abc\"): ");
        assertEquals("7 sinh viên còn lại vẫn được nạp", 7, countRows(out, STUDENT_ROW));
    }

    @Test
    @TC(id = "TC-F03", function = "Khởi động", input = "Tạm đổi tên Students.txt (file không tồn tại)",
        expected = "Không báo lỗi đọc file; chức năng 1 in “No data.”; các dòng Courses.txt đúng định dạng bị bỏ qua vì student not found; chức năng 7 in “No data.”")
    public void TC_F03_Startup_missingStudentsFile_showsNoData() throws IOException {
        DataFiles.deleteStudents();

        String out = runMain("1", "7", "12");

        assertFalse("không được báo lỗi đọc file", out.contains("Cannot read"));
        assertTrue("các dòng Courses.txt đúng định dạng phải bị bỏ qua vì không có sinh viên",
                out.toLowerCase().contains("student not found: stu0002"));
        assertEquals("chức năng 1 và 7 đều in No data.", 2, ConsoleSim.count(out, "No data."));
        assertEquals(0, countRows(out, STUDENT_ROW));
        assertEquals(0, countRows(out, COURSE_ROW));
    }

    // ================================================================ 11. Save / 12. Quit

    @Test
    @TC(id = "TC-F04", function = "11. Save", input = "Chạy TC-C06 rồi chọn 11",
        expected = "In “Saved successfully.”; Courses.txt có thêm dòng CSE301... và các dòng lỗi cũ đã bị loại")
    public void TC_F04_Save_writesNewCourseAndDropsBadLines() throws IOException {
        String out = runMain(concat(ADD_CSE301, "11", "12"));

        assertShows(out, "Saved successfully.");
        List<String> lines = DataFiles.readLines(DataFiles.COURSES);
        assertTrue("có dòng CSE301: " + lines, lines.contains("CSE301, STU0001, Operating Systems, 8, 15/12/2027"));
        assertEquals("7 khóa học cũ + 1 khóa học mới", 8, lines.size());
        for (String l : lines) {
            assertEquals("mọi dòng phải đủ 5 trường: " + l, 5, l.split(",").length);
        }
        assertFalse("dòng lỗi cũ phải bị loại", lines.toString().contains("PRO192-"));
        assertShows(out, "Goodbye!");
    }

    @Test
    @TC(id = "TC-F05", function = "12. Quit", input = "Thêm một khóa học (chưa lưu) rồi chọn 12",
        expected = "Hỏi “You have unsaved changes. Save before quitting? (Y/N):”; chọn Y → lưu, in “Goodbye!” và thoát")
    public void TC_F05_Quit_unsavedChanges_asksAndSavesOnY() throws IOException {
        String out = runMain(concat(ADD_CSE301, "12", "Y"));

        int ask = out.indexOf("You have unsaved changes. Save before quitting? (Y/N): ");
        int saved = out.indexOf("Saved successfully.");
        int bye = out.indexOf("Goodbye!");
        assertTrue("phải hỏi có lưu không", ask >= 0);
        assertTrue("chọn Y -> phải lưu", saved > ask);
        assertTrue("sau khi lưu mới Goodbye", bye > saved);
        assertTrue("file đã có khóa học mới",
                DataFiles.readLines(DataFiles.COURSES).contains("CSE301, STU0001, Operating Systems, 8, 15/12/2027"));
    }

    @Test
    @TC(id = "TC-F06", function = "12. Quit", input = "Không thay đổi gì rồi chọn 12",
        expected = "In “Goodbye!” và thoát ngay, không hỏi lưu")
    public void TC_F06_Quit_noChanges_exitsWithoutAsking() {
        String out = runMain("12");

        assertShows(out, "Goodbye!");
        assertFalse("không được hỏi lưu", out.contains("unsaved changes"));
    }

    // ================================================================ Menu / xác nhận Y/N

    @Test
    @TC(id = "TC-M01", function = "Menu", input = "Nhập 13, 0, abc ở dòng “Choose:”",
        expected = "Mỗi lần in “Enter an integer from 1 to 12.” và hỏi lại")
    public void TC_M01_Menu_invalidChoice_isRejectedAndReprompted() {
        String out = runMain("13", "0", "abc", "12");

        assertEquals("3 lần nhập sai -> 3 thông báo lỗi", 3,
                ConsoleSim.count(out, "Enter an integer from 1 to 12."));
        assertEquals("hỏi lại Choose 4 lần", 4, ConsoleSim.count(out, "Choose: "));
    }

    @Test
    @TC(id = "TC-M02", function = "Xác nhận Y/N", input = "Nhập x khi được hỏi Y/N",
        expected = "In “Enter Y or N.” và hỏi lại")
    public void TC_M02_Confirm_invalidAnswer_isRejectedAndReprompted() {
        String out = runMain(concat(ADD_CSE301, "12", "x", "N"));

        assertEquals(1, ConsoleSim.count(out, "Enter Y or N."));
        assertEquals("hỏi Y/N 2 lần", 2,
                ConsoleSim.count(out, "You have unsaved changes. Save before quitting? (Y/N): "));
        assertShows(out, "Goodbye!");
    }
}
