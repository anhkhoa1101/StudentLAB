package Doubles.Fake;

import core.entities.Course;
import core.interfaces.ICourseDAO;
import utilities.validation.CourseValidation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FAKE DAO cho Course: chạy thật trong RAM (từ chối Course ID trùng như DAO thật).
 * Xem giải thích ở FakeStudentDAO.
 */
public class FakeCourseDAO implements ICourseDAO {
    private final List<Course> data = new ArrayList<>();

    @Override public List<Course> readAll() { return new ArrayList<>(data); }
    @Override public boolean writeAll(List<Course> list) { data.clear(); data.addAll(list); return true; }
    @Override public boolean save() { return true; }
    @Override public boolean add(Course c) {
        if (findByID(c.getCourseId()).isPresent()) return false;
        return data.add(c);
    }
    @Override public boolean update(Course c) { return false; }
    @Override public boolean delete(String id) {
        return data.removeIf(c -> c.getCourseId().equalsIgnoreCase(id));
    }
    @Override public Optional<Course> findByID(String id) {
        for (Course c : data) if (c.getCourseId().equalsIgnoreCase(id)) return Optional.of(c);
        return Optional.empty();
    }
    @Override public List<Course> findByName(String name) { return new ArrayList<>(); }
    @Override public List<Course> findByStudentId(String sid) {
        List<Course> r = new ArrayList<>();
        for (Course c : data) if (c.getStudent().getId().equalsIgnoreCase(sid)) r.add(c);
        return r;
    }

    @Override
    public List<Course> findByStartDate(String input) {
        List<Course> result = new ArrayList<>();
        LocalDate target = CourseValidation.parseLocalDate(input);
        if (target == null) return result;
        for (Course c : data) {
            LocalDate d = c.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (d.equals(target)) result.add(c);
        }
        return result;
    }
}
