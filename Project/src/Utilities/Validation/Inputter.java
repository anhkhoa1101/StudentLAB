package Utilities.Validation;

import java.util.Date;
import java.util.Scanner;
import java.util.function.Predicate;

public class Inputter {

    private static final Scanner SC = new Scanner(System.in);

    private Inputter() {}

    /**
     * Nhập tới khi hợp lệ. allowEmpty=true: Enter trống trả về "" (dùng cho Update).
     * */
    public static String getValid(String msg, Predicate<String> ok, String err, boolean allowEmpty) {
        while (true) {
            System.out.print(msg);
            String s = SC.nextLine().trim();
            if (s.isEmpty() && allowEmpty) return "";
            if (!s.isEmpty() && ok.test(s)) return s;
            System.out.println(err);
        }
    }

    public static String getNonEmpty(String msg) {
        return getValid(msg, s -> true, "Must not be empty.", false);
    }

    /**
     * Integer
     * */

    private static boolean isInt(String s, int min, int max) {
        try { int v = Integer.parseInt(s); return v >= min && v <= max; }
        catch (NumberFormatException e) { return false; }
    }

    public static int getInt(String msg, int min, int max) {
        return Integer.parseInt(getValid(msg, s -> isInt(s, min, max),
                "Enter an integer from " + min + " to " + max + ".", false));
    }

    public static Integer getOptionalInt(String msg, int min, int max) {
        String s = getValid(msg, x -> isInt(x, min, max),
                "Enter an integer from " + min + " to " + max + ".", true);
        return s.isEmpty() ? null : Integer.valueOf(s);
    }

    /**
     * DOUBLE
     * */
    private static boolean isDouble(String s, double min, double max) {
        try { double v = Double.parseDouble(s.replace(',', '.')); return v >= min && v <= max; }
        catch (NumberFormatException e) { return false; }
    }

    public static double getDouble(String msg, double min, double max) {
        return Double.parseDouble(getValid(msg, s -> isDouble(s, min, max),
                "Enter a number from " + min + " to " + max + ".", false).replace(',', '.'));
    }

    public static Double getOptionalDouble(String msg, double min, double max) {
        String s = getValid(msg, x -> isDouble(x, min, max),
                "Enter a number from " + min + " to " + max + ".", true);
        return s.isEmpty() ? null : Double.valueOf(s.replace(',', '.'));
    }

    /**
     * DATE
     * */
    public static Date getDate(String msg, boolean allowEmpty) {
        String s = getValid(msg, x -> CourseValidation.parseDate(x) != null,
                "Invalid date. Format: dd/MM/yyyy.", allowEmpty);
        return s.isEmpty() ? null : CourseValidation.parseDate(s);
    }

    public static boolean confirm(String msg) {
        return getValid(msg + " (Y/N): ",
                s -> s.equalsIgnoreCase("y") || s.equalsIgnoreCase("n"),
                "Enter Y or N.", false).equalsIgnoreCase("y");
    }
}