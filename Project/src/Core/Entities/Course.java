package Core.Entities;

import java.text.SimpleDateFormat;
import java.util.Date;

// Mỗi dòng trong Courses.txt: courseId, studentId, courseName, duration, startDate
public class Course {

    private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

    private String courseId;    // CSE201
    private Student student;   // tham chiếu tới Student (file chỉ lưu studentId)
    private String courseName;
    private int duration;       // cột số nguyên thứ 4 trong file (5..10)
    private Date startDate;     // dd/MM/yyyy

    public Course() {
    }

    public Course(String courseId, Student student, String courseName, int duration, Date startDate) {
        this.courseId = courseId;
        this.student = student;
        this.courseName = courseName;
        this.duration = duration;
        this.startDate = startDate;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    // 1 dòng trong Courses.txt
    public String toDataString() {
        return courseId + ", " + student.getId() + ", " + courseName + ", "
                + duration + ", " + DF.format(startDate);
    }

    // Trùng khi cùng khóa học và cùng sinh viên
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;

        Course c = (Course) o;

        boolean sameCourseId = courseId == null ? c.courseId == null : courseId.equals(c.courseId);

        boolean sameStudent = student == null ? c.student == null : student.equals(c.student);

        return sameCourseId && sameStudent;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (courseId == null ? 0 : courseId.hashCode());
        hash = 67 * hash + (student == null ? 0 : student.hashCode());
        hash = 67 * hash + (courseName == null ? 0 : courseName.hashCode());
        hash = 67 * hash + duration;
        hash = 67 * hash + (startDate == null ? 0 : startDate.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%-8s | %-8s | %-22s | %3d | %s",
                courseId, student.getId(), courseName, duration, DF.format(startDate));
    }
}