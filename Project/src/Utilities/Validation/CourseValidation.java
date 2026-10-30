package Utilities.Validation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.Locale;

public interface CourseValidation {

    int MIN_DURATION = 1;
    int MAX_DURATION = 10;

    // Thứ tự quan trọng: d/M trước, M/d sau
    String[] INPUT_PATTERNS = {
            "d/M/uuuu", "M/d/uuuu",
            "MMM d, uuuu", "MMM d uuuu",      // Sep 9, 2026
            "d MMM, uuuu", "d MMM uuuu",      // 9 Sep, 2026 (sau khi bỏ "th")
            "MMMM d, uuuu", "d MMMM, uuuu", "d MMMM uuuu"  // September 9, 2026
    };

    static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    static boolean isValidDuration(int d) {
        return d >= MIN_DURATION && d <= MAX_DURATION;
    }

    static boolean isFutureDate(Date d) {
        if (d == null) return false;
        LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.isAfter(LocalDate.now());
    }

    /** Nhận nhiều định dạng, trả về LocalDate (null nếu không hợp lệ). */
    static LocalDate parseLocalDate(String s) {
        if (s == null) return null;
        String in = s.trim()
                .replaceAll("(?i)(?<=\\d)(st|nd|rd|th)\\b", "")  // 9th -> 9
                .replaceAll("(?i)\\bsept\\b", "Sep")
                .replaceAll("\\s+", " "); //Xóa bỏ khoảng trắng liên tiếp chuyển thành space thường.
        if (in.isEmpty()) return null;

        for (String p : INPUT_PATTERNS) {
            try {
                DateTimeFormatter f = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern(p)
                        .toFormatter(Locale.ENGLISH)
                        .withResolverStyle(ResolverStyle.STRICT); // 31/02 -> lỗi
                return LocalDate.parse(in, f);
            } catch (DateTimeParseException ignored) { }
        }
        return null;
    }

    /** Giữ nguyên chữ ký cũ để CourseMenu/Inputter không phải sửa. */
    static Date parseDate(String s) {
        LocalDate d = parseLocalDate(s);
        return d == null ? null
                : Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}