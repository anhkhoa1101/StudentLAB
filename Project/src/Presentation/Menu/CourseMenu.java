package Presentation.Menu;

import BusinessObject.CourseList;
import Core.Entities.Course;
import Utilities.Validation.CourseValidation;
import Utilities.Validation.Inputter;
import Utilities.Validation.StudentValidation;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CourseMenu {

    private final CourseList courseList;

    public CourseMenu(CourseList courseList) {
        this.courseList = courseList;
    }

    private String inputStudentId() {
        return Inputter.getValid("Student ID (STU0000): ", StudentValidation::isValidId,
                "Invalid ID. Format: STU0000.", false);
    }

    public void add() {
        String courseId = Inputter.getNonEmpty("Course ID: "); //Nhập Course ID
        String studentId = inputStudentId(); //Nhập StudentID
        String name = Inputter.getNonEmpty("Course name: "); //Nhập tên khóa học
        int duration = Inputter.getInt("Duration (" + CourseValidation.MIN_DURATION + "-"
                        + CourseValidation.MAX_DURATION + " weeks): ",
                CourseValidation.MIN_DURATION, CourseValidation.MAX_DURATION);
        String dateStr = Inputter.getValid("Start date (dd/MM/yyyy, must be in the future): ",
                s -> {
                    Date d = CourseValidation.parseDate(s);
                    return CourseValidation.isFutureDate(d);
                },
                "Invalid date or not a future date. Format: dd/MM/yyyy.", false);

        courseList.add(courseId, studentId, name, duration, CourseValidation.parseDate(dateStr));
        System.out.println("Added successfully (not saved to file yet).");
    }

    /**
     * Chức năng 7: tất cả khóa học, nhóm theo sinh viên.
     * */
    public void listGrouped() {
        List<Course> list = courseList.listGroupedByStudent();
        if (list.isEmpty()) {
            System.out.println("No data.");
            return;
        }
        String current = "";
        for (Course c : list) {
            String sid = c.getStudent().getId();
            if (!sid.equals(current)) {
                current = sid;
                System.out.println("\n== " + sid + " - " + c.getStudent().getName() + " ==");
                System.out.println(String.format("%-8s | %-8s | %-22s | %3s | %s", "CourseID", "Name", "Course Name", "Dur", "Start"));
            }
            System.out.println(c);
        }
    }

    public void totalDuration() {
        String studentId = inputStudentId();
        System.out.println("Total duration of " + studentId.toUpperCase() + ": "
                + courseList.totalDuration(studentId) + " week(s).");
    }

    public void print(Map<String, Long> map) {
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            System.out.println(
                    entry.getKey() + " : " + entry.getValue()
            );
        }
    }
}