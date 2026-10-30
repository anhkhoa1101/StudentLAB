package Doubles.Mock;

import core.entities.Student;
import core.interfaces.IStudentDAO;

import Support.TestLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MOCK DAO: DAO "diễn kịch" (khác với Doubles.Fake.FakeStudentDAO):
 *  - FakeStudentDAO  = chạy thật trong RAM, có dữ liệu thật -> kiểm tra KẾT QUẢ.
 *  - MockStudentDAO  = trả đúng giá trị mà test cấu hình sẵn (STUB) và ghi lại mọi lần
 *                      bị gọi (RECORD) -> kiểm tra TƯƠNG TÁC (lớp nghiệp vụ có gọi DAO
 *                      đúng cách, đúng số lần, đúng tham số không).
 *
 * Cách dùng trong test:
 *   1) Cấu hình (stub):  dao.saveResult = false;  dao.findStub.put("STU0001", student);
 *   2) Gọi lớp cần test.
 *   3) Kiểm tra (verify): assertEquals(1, dao.saveCalls);  assertTrue(dao.addCalls.isEmpty());
 */
public class MockStudentDAO implements IStudentDAO {

    // ---------- STUB: giá trị trả về do test quyết định ----------
    public List<Student> readAllStub = new ArrayList<>();
    public final Map<String, Student> findStub = new HashMap<>();   // key = id chính xác được truyền vào findByID
    public boolean addResult = true;
    public boolean updateResult = true;
    public boolean deleteResult = true;
    public boolean saveResult = true;

    // ---------- RECORD: nhật ký các lần bị gọi ----------
    public int readAllCalls = 0;
    public int writeAllCalls = 0;
    public int saveCalls = 0;
    public final List<Student> addCalls = new ArrayList<>();
    public final List<Student> updateCalls = new ArrayList<>();
    public final List<String> deleteCalls = new ArrayList<>();
    public final List<String> findCalls = new ArrayList<>();

    /** Tổng số lần ghi/sửa/xóa/lưu: dùng để khẳng định "DAO không bị đụng tới". */
    public int writeCallsTotal() {
        return addCalls.size() + updateCalls.size() + deleteCalls.size() + writeAllCalls + saveCalls;
    }

    @Override public List<Student> readAll() {
        readAllCalls++;
        TestLog.output("StudentDAO.readAll()");
        return new ArrayList<>(readAllStub);         // trả bản sao như DAO thật
    }

    @Override public boolean writeAll(List<Student> list) {
        writeAllCalls++;
        TestLog.output("StudentDAO.writeAll()");
        return true;
    }

    @Override public boolean save() {
        saveCalls++;
        TestLog.output("StudentDAO.save() -> " + saveResult);
        return saveResult;
    }

    @Override public boolean add(Student s) {
        addCalls.add(s);
        TestLog.output("StudentDAO.add(" + s.getId() + ") -> " + addResult);
        return addResult;
    }

    @Override public boolean update(Student s) {
        updateCalls.add(s);
        TestLog.output("StudentDAO.update(" + s.getId() + ") -> " + updateResult);
        return updateResult;
    }

    @Override public boolean delete(String id) {
        deleteCalls.add(id);
        TestLog.output("StudentDAO.delete(" + id + ") -> " + deleteResult);
        return deleteResult;
    }

    @Override public Optional<Student> findByID(String id) {
        findCalls.add(id);
        TestLog.output("StudentDAO.findByID(" + id + ") -> " + (findStub.containsKey(id) ? "found" : "empty"));
        return Optional.ofNullable(findStub.get(id));
    }
}
