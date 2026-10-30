package Support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mô tả 1 Test Case (lấy từ bảng Test Case trong file .md).
 * Test không gắn @TC vẫn được ghi log: mã TC = tên method, chức năng = tên lớp test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TC {
    String id();
    String function();
    String input();       // mô tả dữ liệu nhập (cột "Dữ liệu nhập" của bảng TC)
    String expected();    // kết quả mong đợi
}
