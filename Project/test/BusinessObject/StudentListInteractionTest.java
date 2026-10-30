package businessObject;

import core.entities.Course;
import core.entities.Student;
import Doubles.Mock.MockCourseDAO;
import Doubles.Mock.MockStudentDAO;
import Support.CsvLogger;
import Support.TestLog;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * INTERACTION TEST cho StudentList (dùng MOCK DAO): "StudentList có gọi DAO đúng cách không?"
 *   - gọi add/update/delete/save đúng số lần, đúng tham số?
 *   - dữ liệu sai thì có DỪNG trước khi đụng tới DAO không?
 *   - DAO báo thất bại (save() = false) thì StudentList xử lý ra sao?
 * (Kiểm tra kết quả cuối cùng với DAO chạy thật -> StudentListStateTest.)
 */
public class StudentListInteractionTest {

    @Rule
    public final CsvLogger tcLog = new CsvLogger();

    private MockStudentDAO studentDAO;
    private MockCourseDAO courseDAO;
    private StudentList studentList;

    @Before
    public void setUp() {
        studentDAO = new MockStudentDAO();
        courseDAO = new MockCourseDAO();
        studentList = new StudentList(studentDAO, courseDAO);
    }

    private static Student student(String id, String name, String major, double gpa) {
        TestLog.input("Student(" + id + ", " + name + ", " + major + ", " + gpa + ")");
        return new Student(id, name, major, gpa);
    }

    // ================================================================ add

    @Test
    public void add_validStudent_checksDuplicateThenAddsAndSavesOnce() {
        Student s = student("stu0001", "Nguyen Van A", "SE", 3.5);

        assertTrue(studentList.add(s));

        assertEquals("kiểm tra trùng bằng ID đã chuẩn hóa", Arrays.asList("STU0001"), studentDAO.findCalls);
        assertEquals(1, studentDAO.addCalls.size());
        assertSame(s, studentDAO.addCalls.get(0));
        assertEquals("ID phải được viết hoa trước khi lưu", "STU0001", studentDAO.addCalls.get(0).getId());
        assertEquals("add xong phải save đúng 1 lần", 1, studentDAO.saveCalls);
    }

    @Test
    public void add_invalidId_stopsBeforeTouchingDao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> studentList.add(student("ABC123", "Nguyen Van A", "SE", 3.5)));

        assertTrue(e.getMessage().contains("Invalid ID"));
        assertTrue("không được tra cứu DAO", studentDAO.findCalls.isEmpty());
        assertEquals("không được ghi gì", 0, studentDAO.writeCallsTotal());
    }

    @Test
    public void add_invalidName_stopsBeforeTouchingDao() {
        assertThrows(IllegalArgumentException.class,
                () -> studentList.add(student("STU0001", "Nguyen123", "SE", 3.5)));
        assertEquals(0, studentDAO.writeCallsTotal());
    }

    @Test
    public void add_gpaOutOfRange_stopsBeforeTouchingDao() {
        assertThrows(IllegalArgumentException.class,
                () -> studentList.add(student("STU0001", "Nguyen Van A", "SE", 4.5)));
        assertEquals(0, studentDAO.writeCallsTotal());
    }

    @Test
    public void add_nullStudent_throws() {
        assertThrows(IllegalArgumentException.class, () -> studentList.add(null));
        assertEquals(0, studentDAO.writeCallsTotal());
    }

    @Test
    public void add_duplicateId_throwsAndDoesNotAddOrSave() {
        studentDAO.findStub.put("STU0001", student("STU0001", "Old Name", "SE", 3.0));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> studentList.add(student("STU0001", "Nguyen Van A", "SE", 3.5)));

        assertEquals("Student ID already exists: STU0001", e.getMessage());
        assertTrue(studentDAO.addCalls.isEmpty());
        assertEquals(0, studentDAO.saveCalls);
    }

    @Test
    public void add_whenDaoSaveFails_returnsFalse() {
        studentDAO.saveResult = false;

        assertFalse(studentList.add(student("STU0001", "Nguyen Van A", "SE", 3.5)));

        assertEquals("vẫn đã gọi add", 1, studentDAO.addCalls.size());
        assertEquals(1, studentDAO.saveCalls);
    }

    // ================================================================ update

    @Test
    public void update_existingStudent_callsUpdateAndSaveOnce() {
        studentDAO.findStub.put("STU0001", student("STU0001", "Nguyen Van A", "SE", 3.5));
        Student changed = student("STU0001", "Nguyen Van B", "IS", 3.0);

        assertTrue(studentList.update(changed));

        assertEquals(1, studentDAO.updateCalls.size());
        assertSame(changed, studentDAO.updateCalls.get(0));
        assertEquals(1, studentDAO.saveCalls);
        assertTrue("update không được gọi add", studentDAO.addCalls.isEmpty());
    }

    @Test
    public void update_unknownStudent_throwsWithoutUpdating() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> studentList.update(student("STU0001", "Nguyen Van B", "IS", 3.0)));

        assertEquals("Student not found: STU0001", e.getMessage());
        assertTrue(studentDAO.updateCalls.isEmpty());
        assertEquals(0, studentDAO.saveCalls);
    }

    @Test
    public void update_invalidGpa_stopsBeforeTouchingDao() {
        assertThrows(IllegalArgumentException.class,
                () -> studentList.update(student("STU0001", "Nguyen Van B", "IS", -1)));
        assertTrue(studentDAO.findCalls.isEmpty());
        assertEquals(0, studentDAO.writeCallsTotal());
    }

    // ================================================================ delete

    @Test
    public void delete_enrolledStudent_isBlockedBeforeDaoDelete() {
        Student s = student("STU0001", "Nguyen Van A", "SE", 3.5);
        studentDAO.findStub.put("STU0001", s);
        courseDAO.readAllStub.add(new Course("PRJ301", s, "Java Web", 6, new Date()));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> studentList.delete("STU0001"));

        assertEquals("Cannot delete: student is enrolled in course(s).", e.getMessage());
        assertTrue("không được xóa", studentDAO.deleteCalls.isEmpty());
        assertEquals("không được save", 0, studentDAO.saveCalls);
    }

    @Test
    public void delete_studentWhoseCoursesBelongToOthers_deletesAndSavesOnce() {
        Student s1 = student("STU0001", "Nguyen Van A", "SE", 3.5);
        Student s2 = student("STU0002", "Tran Thi B", "IS", 3.0);
        studentDAO.findStub.put("STU0001", s1);
        courseDAO.readAllStub.add(new Course("PRJ301", s2, "Java Web", 6, new Date()));

        assertTrue(studentList.delete("STU0001"));

        assertEquals(Arrays.asList("STU0001"), studentDAO.deleteCalls);
        assertEquals(1, studentDAO.saveCalls);
        assertEquals("phải tra danh sách khóa học đúng 1 lần", 1, courseDAO.readAllCalls);
    }

    @Test
    public void delete_unknownStudent_doesNotEvenCheckCourses() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> studentList.delete("STU0099"));

        assertEquals("Student not found: STU0099", e.getMessage());
        assertEquals(0, courseDAO.readAllCalls);
        assertTrue(studentDAO.deleteCalls.isEmpty());
    }

    @Test
    public void delete_whenDaoSaveFails_returnsFalse() {
        studentDAO.findStub.put("STU0001", student("STU0001", "Nguyen Van A", "SE", 3.5));
        studentDAO.saveResult = false;

        assertFalse(studentList.delete("STU0001"));
        assertEquals(1, studentDAO.deleteCalls.size());
    }

    // ================================================================ truy vấn (đọc dữ liệu do mock cấu hình sẵn)

    @Test
    public void countByMajor_groupsAndSortsByMajor_withoutWriting() {
        studentDAO.readAllStub = Arrays.asList(
                student("STU0001", "An Binh", "SE", 3.0),
                student("STU0002", "Cao Dung", "IS", 3.2),
                student("STU0003", "Em Giang", "SE", 3.4));

        Map<String, Long> r = studentList.countByMajor();

        assertEquals(Arrays.asList("IS", "SE"), new ArrayList<>(r.keySet()));
        assertEquals(Long.valueOf(1), r.get("IS"));
        assertEquals(Long.valueOf(2), r.get("SE"));
        assertEquals(1, studentDAO.readAllCalls);
        assertEquals(0, studentDAO.writeCallsTotal());
    }

    @Test
    public void countByMajor_emptyData_returnsEmptyMap() {
        assertTrue(studentList.countByMajor().isEmpty());
    }

    @Test
    public void topGpa_returnsEveryStudentTiedForTop() {
        studentDAO.readAllStub = Arrays.asList(
                student("STU0001", "An Binh", "SE", 3.9),
                student("STU0002", "Cao Dung", "IS", 3.0),
                student("STU0003", "Em Giang", "SE", 3.9));

        List<Student> top = studentList.topGpa();

        assertEquals(2, top.size());
        assertEquals("STU0001", top.get(0).getId());
        assertEquals("STU0003", top.get(1).getId());
    }

    @Test
    public void topGpa_emptyData_returnsEmptyList() {
        assertTrue(studentList.topGpa().isEmpty());
    }

    @Test
    public void findByMajor_isCaseInsensitiveSubstring() {
        studentDAO.readAllStub = Arrays.asList(
                student("STU0001", "An Binh", "Software Engineering", 3.0),
                student("STU0002", "Cao Dung", "Information Systems", 3.2));

        List<Student> r = studentList.findByMajor("  softWARE ");

        assertEquals(1, r.size());
        assertEquals("STU0001", r.get(0).getId());
    }

    @Test
    public void searchByName_blankOrNull_returnsEmptyWithoutReadingDao() {
        assertTrue(studentList.searchByName("   ").isEmpty());
        assertTrue(studentList.searchByName(null).isEmpty());
        assertEquals("không cần đọc DAO", 0, studentDAO.readAllCalls);
    }

    @Test
    public void sortByGpaAsc_sortsAscending_andLeavesDaoDataUntouched() {
        List<Student> stub = new ArrayList<>(Arrays.asList(
                student("STU0001", "An Binh", "SE", 3.9),
                student("STU0002", "Cao Dung", "SE", 2.1),
                student("STU0003", "Em Giang", "SE", 3.0)));
        studentDAO.readAllStub = stub;

        List<Student> sorted = studentList.sortByGpaAsc();

        assertEquals(Arrays.asList("STU0002", "STU0003", "STU0001"),
                Arrays.asList(sorted.get(0).getId(), sorted.get(1).getId(), sorted.get(2).getId()));
        assertEquals("dữ liệu gốc của DAO không bị đổi thứ tự", "STU0001", stub.get(0).getId());
    }

    // ================================================================ save

    @Test
    public void save_delegatesToDaoAndReturnsItsResult() {
        assertTrue(studentList.save());
        studentDAO.saveResult = false;
        assertFalse(studentList.save());
        assertEquals(2, studentDAO.saveCalls);
    }

    @Test
    public void newStudentList_isNotDirtyAndReadsNothing() {
        assertFalse(studentList.isDirty());
        assertEquals(0, studentDAO.readAllCalls);
        assertEquals(Collections.emptyList(), studentList.getAll());
    }
}
