package Support;

import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * JUnit Rule dành cho test chạy Program.main thật (TC-F, TC-M).
 *
 * Program.main đọc/ghi 2 file cố định src/DataObject/data/Students.txt và Courses.txt
 * (đường dẫn tương đối so với thư mục gốc dự án). Rule này:
 *   - TRƯỚC mỗi test : sao lưu 2 file thật, rồi ghi dữ liệu GỐC (có dòng lỗi) để test có đầu vào cố định.
 *   - SAU mỗi test   : khôi phục đúng 2 file thật (nếu trước đó không có file thì xóa).
 * Nhờ vậy test Save / Quit không làm hỏng dữ liệu của bạn.
 */
public class DataFiles extends ExternalResource {

    public static final Path STUDENTS = Paths.get("src/DataObject/data/Students.txt");
    public static final Path COURSES = Paths.get("src/DataObject/data/Courses.txt");

    /** Students.txt gốc: 7 sinh viên. */
    public static final String ORIGINAL_STUDENTS =
            "STU0001, Nguyen Van A, Software Engineering, 3.4\n"
          + "STU0002, Tran Thi B, Information Systems, 3.8\n"
          + "STU0003, Nguyen Van An, Software Engineering, 3.4\n"
          + "STU0004, Tran Thi Bich, Information Systems, 3.7\n"
          + "STU0005, Le Hoang Minh, Computer Science, 3.2\n"
          + "STU0006, Pham Quoc Huy, Artificial Intelligence, 3.9\n"
          + "STU0007, Vo Thanh Tam, Data Science, 3.5\n";

    /**
     * Courses.txt gốc: 11 dòng, dòng 3 / 7 / 10 sai định dạng, dòng 8 trùng CSE201
     * -> nạp được đúng 7 khóa học.
     */
    public static final String ORIGINAL_COURSES =
            "CSE201, STU0002, Database Systems, 10, 15/09/2025\n"
          + "AI401, STU0004, Machine Learning, 10, 05/10/2025\n"
          + "MAT301, STU0003 Discrete Mathematics, 8 20/09/2025\n"
          + "DS501, STU0005, Big Data Analytics, 8, 10/10/2025\n"
          + "DBI202, STU0002, Database Design, 5, 01/11/2025\n"
          + "MAT301, STU0003, Discrete Mathematics, 8, 20/09/2025\n"
          + "PRJ301 STU0001, Java Web Project, 6; 20/10/2025\n"
          + "CSE201, STU0002, Database Systems, 10, 15/09/2025\n"
          + "PRJ301, STU0001, Java Web Project, 6, 20/10/2025\n"
          + "PRO192- STU0003- Java Programming- 8- 25/10/2025\n"
          + "PRO192, STU0003, Java Programming, 8, 25/10/2025\n";

    private byte[] oldStudents;
    private byte[] oldCourses;

    @Override protected void before() throws Throwable {
        oldStudents = readOrNull(STUDENTS);
        oldCourses = readOrNull(COURSES);
        Files.createDirectories(STUDENTS.toAbsolutePath().getParent());
        write(STUDENTS, ORIGINAL_STUDENTS);
        write(COURSES, ORIGINAL_COURSES);
    }

    @Override protected void after() {
        try {
            restore(STUDENTS, oldStudents);
            restore(COURSES, oldCourses);
        } catch (IOException e) {
            System.err.println("[DataFiles] Cannot restore data files: " + e.getMessage());
        }
    }

    // ---- dùng trong test để đổi đầu vào ----

    public static void write(Path p, String content) throws IOException {
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void deleteStudents() throws IOException {
        Files.deleteIfExists(STUDENTS);
    }

    public static List<String> readLines(Path p) throws IOException {
        return Files.readAllLines(p, StandardCharsets.UTF_8);
    }

    private static byte[] readOrNull(Path p) throws IOException {
        return Files.exists(p) ? Files.readAllBytes(p) : null;
    }

    private static void restore(Path p, byte[] old) throws IOException {
        if (old == null) Files.deleteIfExists(p);
        else Files.write(p, old);
    }
}
