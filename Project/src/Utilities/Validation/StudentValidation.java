package Utilities.Validation;

public interface StudentValidation {

    String ID_PATTERN   = "^STU\\d{4}$";
    String NAME_PATTERN = "^\\p{L}+(\\s\\p{L}+)*$";   // chữ cái + khoảng trắng (có dấu tiếng Việt)
    double MIN_GPA = 0.0;
    double MAX_GPA = 4.0;

    static boolean isValidId(String id) {
        return id != null && BaseValidation.isValid(id.trim().toUpperCase(), ID_PATTERN);
    }

    static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2
                && BaseValidation.isValid(name.trim(), NAME_PATTERN);
    }

    static boolean isValidMajor(String major) {
        return major != null && !major.trim().isEmpty();
    }

    static boolean isValidGpa(double gpa) {
        return gpa >= MIN_GPA && gpa <= MAX_GPA;
    }
}