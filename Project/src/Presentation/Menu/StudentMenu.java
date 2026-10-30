package Presentation.Menu;

import BusinessObject.StudentList;
import Core.Entities.Student;
import Utilities.Validation.Inputter;
import Utilities.Validation.StudentValidation;

import java.util.List;
import java.util.Collections;

public class StudentMenu {

    private final StudentList studentList;

    public StudentMenu(StudentList studentList) {
        this.studentList = studentList;
    }

    private String inputId(String msg) {
        return Inputter.getValid(msg, StudentValidation::isValidId,
                "Invalid ID. Format: STU0000.", false);
    }

    public void listAll() { print(studentList.getAll()); }

    public void add() {
        String id = inputId("ID (STU0000): ");
        String name = Inputter.getValid("Name: ", StudentValidation::isValidName,
                "Invalid name (letters and spaces only).", false);
        String major = Inputter.getNonEmpty("Major: ");
        double gpa = Inputter.getDouble("GPA (0.0 - 4.0): ",
                StudentValidation.MIN_GPA, StudentValidation.MAX_GPA);
        System.out.println(studentList.add(new Student(id, name, major, gpa))
                ? "Added successfully (not saved to file yet)." : "Student ID already exists.");
    }


    public void searchById() {
        Student s = studentList.findById(inputId("ID to search: ")).orElse(null);
        if (s == null) System.out.println("Student not found.");
        else print(Collections.singletonList(s));
    }

    public void update() {
        Student old = studentList.findById(inputId("ID to update: ")).orElse(null);
        if (old == null) {
            System.out.println("Student not found.");
            return;
        }
        System.out.println("Current: " + old);
        System.out.println("(Press Enter to keep the current value)");
        String name = Inputter.getValid("New name: ", StudentValidation::isValidName, "Invalid name.", true);
        String major = Inputter.getValid("New major: ", StudentValidation::isValidMajor, "Invalid major.", true);
        Double gpa = Inputter.getOptionalDouble("New GPA: ", StudentValidation.MIN_GPA, StudentValidation.MAX_GPA);

        Student updated = new Student(old.getId(),
                name.isEmpty() ? old.getName() : name,
                major.isEmpty() ? old.getMajor() : major,
                gpa == null ? old.getGpa() : gpa);
        System.out.println(studentList.update(updated)
                ? "Updated successfully (not saved to file yet)." : "Student not found.");
    }

    public void listByMajor() {
        print(studentList.findByMajor(Inputter.getNonEmpty("Major: ")));
    }

    public void remove() {
        String id = inputId("ID to remove: ");
        if (Inputter.confirm("Remove " + id.toUpperCase() + "?")) {
            System.out.println(studentList.delete(id)
                    ? "Removed successfully (not saved to file yet)." : "Student not found.");
        }
    }

    public void sortByGpa() { print(studentList.sortByGpaAsc()); }

    private void print(List<Student> list) {
        if (list.isEmpty()) {
            System.out.println("No data.");
            return;
        }
        System.out.println(String.format("%-8s | %-25s | %-25s | %s", "ID", "Name", "Major", "GPA"));
        System.out.println("---------+----------------------+---------------------------+-----");
        list.forEach(System.out::println);
    }


}