package Doubles.Mock;

import core.entities.Course;
import core.interfaces.ICourseDAO;

import Support.TestLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MOCK DAO cho Course: stub giá trị trả về + ghi lại các lần bị gọi.
 * Xem giải thích ở MockStudentDAO.
 */
public class MockCourseDAO implements ICourseDAO {

    // ---------- STUB ----------
    public List<Course> readAllStub = new ArrayList<>();
    public List<Course> findByStudentIdStub = new ArrayList<>();
    public boolean addResult = true;
    public boolean saveResult = true;

    // ---------- RECORD ----------
    public int readAllCalls = 0;
    public int saveCalls = 0;
    public final List<Course> addCalls = new ArrayList<>();
    public final List<String> findByStudentIdCalls = new ArrayList<>();

    @Override public List<Course> readAll() {
        readAllCalls++;
        TestLog.output("CourseDAO.readAll()");
        return new ArrayList<>(readAllStub);
    }

    @Override public boolean writeAll(List<Course> list) { return true; }

    @Override public boolean save() {
        saveCalls++;
        TestLog.output("CourseDAO.save() -> " + saveResult);
        return saveResult;
    }

    @Override public boolean add(Course c) {
        addCalls.add(c);
        TestLog.output("CourseDAO.add(" + c.getCourseId() + ") -> " + addResult);
        return addResult;
    }

    @Override public boolean update(Course c) { return false; }

    @Override public boolean delete(String id) { return false; }

    @Override public Optional<Course> findByID(String id) { return Optional.empty(); }

    @Override public List<Course> findByName(String name) { return new ArrayList<>(); }

    @Override public List<Course> findByStudentId(String studentId) {
        findByStudentIdCalls.add(studentId);
        TestLog.output("CourseDAO.findByStudentId(" + studentId + ")");
        return new ArrayList<>(findByStudentIdStub);
    }

    @Override public List<Course> findByStartDate(String input) { return new ArrayList<>(); }
}
