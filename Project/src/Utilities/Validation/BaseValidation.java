package Utilities.Validation;

public interface BaseValidation {

    static boolean isValid(String value, String pattern) {
        if (value == null || pattern == null) {
            return false;
        }

        return value.matches(pattern);
    }
}
