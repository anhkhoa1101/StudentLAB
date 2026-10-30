package Presentation;

import BusinessObject.CourseList;
import BusinessObject.StudentList;
import DataObject.DAO.CourseDAO;
import DataObject.DAO.StudentDAO;
import Presentation.Menu.CourseMenu;
import Presentation.Menu.StudentMenu;
import Utilities.Validation.Inputter;

public class Program {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();
        CourseDAO courseDAO = new CourseDAO(studentDAO);

        StudentList studentList = new StudentList(studentDAO, courseDAO);
        CourseList courseList = new CourseList(courseDAO, studentDAO);
        StudentMenu sm = new StudentMenu(studentList);
        CourseMenu cm = new CourseMenu(courseList);

        int choice;
        do {
            System.out.println("\n========== MENU ==========");
            System.out.println(" 1. List all students");
            System.out.println(" 2. Add a new student");
            System.out.println(" 3. Search for a student by ID");
            System.out.println(" 4. Update a student by ID");
            System.out.println(" 5. List all students by major");
            System.out.println(" 6. Add a new course");
            System.out.println(" 7. List all courses by student (grouped)");
            System.out.println(" 8. Calculate total study duration by student ID");
            System.out.println(" 9. Remove a student by ID");
            System.out.println("10. Sort students by GPA");
            System.out.println("11. Save data to files");
            System.out.println("12. Quit program");
            choice = Inputter.getInt("Choose: ", 1, 13);

            try {
                switch (choice) {
                    case 1:  sm.listAll(); break;
                    case 2:  sm.add(); break;
                    case 3:  sm.searchById(); break;
                    case 4:  sm.update(); break;
                    case 5:  sm.listByMajor(); break;
                    case 6:  cm.add(); break;
                    case 7:  cm.listGrouped(); break;
                    case 8:  cm.totalDuration(); break;
                    case 9:  sm.remove(); break;
                    case 10: sm.sortByGpa(); break;
                    case 11: saveAll(studentList, courseList); break;

                    case 12:
                        if ((studentList.isDirty() || courseList.isDirty())
                                && Inputter.confirm("You have unsaved changes. Save before quitting?")) {
                            saveAll(studentList, courseList);
                        }
                        System.out.println("Goodbye!");
                        break;
                    default: break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (choice != 12);
    }

    private static void saveAll(StudentList sl, CourseList cl) {
        boolean s = sl.save();
        boolean c = cl.save();
        System.out.println(s && c ? "Saved successfully."
                : "Failed to save: " + (!s ? "Students.txt " : "") + (!c ? "Courses.txt" : ""));
    }
}