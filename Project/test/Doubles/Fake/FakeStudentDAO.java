package Doubles.Fake;

import core.entities.Student;
import core.interfaces.IStudentDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FAKE DAO: DAO "chạy thật" trong bộ nhớ (RAM), có dữ liệu thật, không đụng tới file.
 * Dùng cho test KẾT QUẢ (state): thêm xong thì có tìm thấy không? danh sách có đúng không?
 * Cặp đối lập: Doubles.Mock.MockStudentDAO (kiểm tra TƯƠNG TÁC).
 */
public class FakeStudentDAO implements IStudentDAO {
    private final List<Student> data = new ArrayList<>();
    public boolean saved = false;

    @Override public List<Student> readAll() { return new ArrayList<>(data); }
    @Override public boolean writeAll(List<Student> list) { data.clear(); data.addAll(list); return true; }
    @Override public boolean save() { saved = true; return true; }
    @Override public boolean add(Student s) { return data.add(s); }
    @Override public boolean update(Student s) {
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).getId().equalsIgnoreCase(s.getId())) { data.set(i, s); return true; }
        return false;
    }
    @Override public boolean delete(String id) {
        return data.removeIf(s -> s.getId().equalsIgnoreCase(id));
    }
    @Override public Optional<Student> findByID(String id) {
        for (Student s : data) if (s.getId().equalsIgnoreCase(id)) return Optional.of(s);
        return Optional.empty();
    }
}
