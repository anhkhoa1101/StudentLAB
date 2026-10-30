package Support;

import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JUnit Rule tự động ghi log Test Case ra CSV. Dùng bằng 1 dòng trong lớp test:
 *
 *     @Rule public final CsvLogger tcLog = new CsvLogger();
 *
 * Mỗi lớp test có 1 file riêng:  test-results/<TênLớpTest>_Log.csv
 * (đổi thư mục bằng -Dtc.log.dir=...). File có BOM UTF-8 để Excel đọc đúng tiếng Việt.
 *
 * Luồng cho MỖI test:
 *   starting()   -> reset sổ ghi, bật bàn phím giả + bắt màn hình, ghi giờ bắt đầu
 *   (test chạy)  -> ConsoleSim / TestLog thu Input & Output
 *   succeeded() / failed() -> chốt Pass / Fail (+ lý do) và ghi dòng vào CSV
 *   finished()   -> trả System.out về như cũ
 * Test chưa chạy có trạng thái NOT RUN.
 *
 * Ghi đè nhưng KHÔNG làm mất kết quả cũ:
 *   - Khi lớp test được nạp lần đầu, nếu file CSV đã có thì đọc kết quả cũ vào trước;
 *     chỉ các dòng vừa chạy mới bị cập nhật, dòng chưa chạy giữ kết quả + "Thời điểm chạy" cũ.
 *     (Chạy riêng 1 test không còn biến các TC khác thành NOT RUN.)
 *   - Mỗi lần 1 test chạy xong còn được NỐI THÊM 1 dòng vào test-results/_History.csv
 *     (chỉ nối thêm, không ghi đè) để so sánh trước/sau khi sửa code.
 */
public class CsvLogger extends TestWatcher {

    static final String[] HEADER = {
        "Mã TC", "Chức năng", "Dữ liệu nhập (mô tả)", "Kết quả mong đợi",
        "Input thực tế (phím đã gõ)", "Output thực tế (chương trình in)", "Màn hình console (prompt + phím gõ)",
        "Trạng thái", "Thời gian (ms)", "Thời điểm chạy", "Ghi chú"
    };
    private static final int C_INPUT = 4, C_OUTPUT = 5, C_CONSOLE = 6, C_STATUS = 7,
                             C_MS = 8, C_TIME = 9, C_NOTE = 10;
    private static final String SEP = " ⏎ ";    // chỉ dùng cho _History.csv (mỗi bản ghi 1 dòng)
    private static final String NL = "\n";       // xuống dòng thật bên trong ô CSV (ô được đặt trong dấu ")

    /** tên lớp test -> (mã TC -> dòng log) */
    private static final Map<String, Map<String, String[]>> FILES = new LinkedHashMap<>();

    private long startedAt;

    @Override protected void starting(Description d) {
        ensureRows(d.getTestClass());
        TestLog.reset();
        ConsoleSim.begin();
        startedAt = System.nanoTime();
    }

    @Override protected void succeeded(Description d) {
        record(d, "Pass", "");
    }

    @Override protected void failed(Throwable e, Description d) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        record(d, "Fail", e.getClass().getSimpleName() + (msg.isEmpty() ? "" : ": " + msg));
    }

    @Override protected void finished(Description d) {
        ConsoleSim.end();
    }

    // ------------------------------------------------------------------ nội bộ

    private void record(Description d, String status, String note) {
        long ms = (System.nanoTime() - startedAt) / 1_000_000;
        Class<?> cls = d.getTestClass();
        synchronized (CsvLogger.class) {
            String[] row = rowFor(cls, d);
            List<String> in = new ArrayList<>(ConsoleSim.typed());
            in.addAll(TestLog.inputs());
            List<String> out = new ArrayList<>();
            String screen = ConsoleSim.output().trim();
            if (!screen.isEmpty()) out.addAll(ConsoleSim.lines(screen));
            out.addAll(TestLog.outputs());

            row[C_INPUT] = String.join(NL, in);
            row[C_OUTPUT] = String.join(NL, out);
            row[C_CONSOLE] = ConsoleSim.transcript().trim();
            row[C_STATUS] = status;
            row[C_MS] = String.valueOf(ms);
            row[C_TIME] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            int unused = ConsoleSim.unusedInput();
            row[C_NOTE] = note + (unused > 0 ? (note.isEmpty() ? "" : " | ") + "Còn thừa " + unused
                    + " dòng input chưa được chương trình đọc" : "");
            write(cls);
            appendHistory(cls, row);
        }
    }

    private static String idOf(Description d) {
        TC tc = d.getAnnotation(TC.class);
        return tc != null ? tc.id() : d.getMethodName();
    }

    private static String[] rowFor(Class<?> cls, Description d) {
        Map<String, String[]> rows = FILES.get(cls.getName());
        String id = idOf(d);
        String[] row = rows.get(id);
        if (row == null) {                       // test parameterized hoặc tên lạ: tạo dòng mới
            row = newRow(id, cls.getSimpleName(), "", "");
            rows.put(id, row);
        }
        return row;
    }

    private static String[] newRow(String id, String function, String input, String expected) {
        String[] r = new String[HEADER.length];
        java.util.Arrays.fill(r, "");
        r[0] = id; r[1] = function; r[2] = input; r[3] = expected;
        r[C_STATUS] = "NOT RUN";
        return r;
    }

    /** Tạo sẵn bảng (trạng thái NOT RUN) từ tất cả method @Test của lớp, theo thứ tự mã TC. */
    private static synchronized void ensureRows(Class<?> cls) {
        if (FILES.containsKey(cls.getName())) return;
        Map<String, String[]> rows = new java.util.TreeMap<>();
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getAnnotation(Test.class) == null) continue;
            TC tc = m.getAnnotation(TC.class);
            String[] r = tc != null
                    ? newRow(tc.id(), tc.function(), tc.input(), tc.expected())
                    : newRow(m.getName(), cls.getSimpleName(), "", "");
            rows.put(r[0], r);
        }
        carryOverOldResults(cls, rows);
        FILES.put(cls.getName(), rows);
        write(cls);
    }

    private static Path fileOf(Class<?> cls) {
        return Paths.get(System.getProperty("tc.log.dir", "test-results"), cls.getSimpleName() + "_Log.csv");
    }

    /** Đọc file CSV cũ (nếu có) và chép kết quả cũ sang các dòng cùng mã TC. */
    private static void carryOverOldResults(Class<?> cls, Map<String, String[]> rows) {
        Path file = fileOf(cls);
        if (!Files.exists(file)) return;
        try {
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
            boolean first = true;
            for (List<String> old : parseCsv(text)) {
                if (first) { first = false; continue; }                 // bỏ dòng tiêu đề
                if (old.size() != HEADER.length) continue;              // file khác định dạng: bỏ qua
                String[] row = rows.get(old.get(0));
                if (row == null) continue;                              // test đã bị xóa
                for (int i = C_INPUT; i < HEADER.length; i++) row[i] = old.get(i);
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("[CsvLogger] Cannot read old results " + file + ": " + e.getMessage());
        }
    }

    /** Parser CSV tối thiểu: ô đặt trong dấu ", "" là dấu " thật, dòng kết thúc bằng \r\n hoặc \n. */
    private static List<List<String>> parseCsv(String text) {
        List<List<String>> out = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') { cell.append('"'); i++; }
                    else quoted = false;
                } else cell.append(c);
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                row.add(cell.toString()); cell.setLength(0);
            } else if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                row.add(cell.toString()); cell.setLength(0);
                out.add(row); row = new ArrayList<>();
            } else cell.append(c);
        }
        if (cell.length() > 0 || !row.isEmpty()) { row.add(cell.toString()); out.add(row); }
        return out;
    }

    private static final String[] HISTORY_HEADER = {
        "Thời điểm chạy", "Lớp test", "Mã TC", "Trạng thái", "Thời gian (ms)", "Ghi chú"
    };

    /** Nối thêm 1 dòng vào test-results/_History.csv (không bao giờ ghi đè). */
    private static void appendHistory(Class<?> cls, String[] row) {
        Path file = Paths.get(System.getProperty("tc.log.dir", "test-results"), "_History.csv");
        try {
            Path dir = file.toAbsolutePath().getParent();
            if (dir != null) Files.createDirectories(dir);
            StringBuilder sb = new StringBuilder();
            boolean isNew = !Files.exists(file);
            if (isNew) appendRow(sb, HISTORY_HEADER, false);
            appendRow(sb, new String[] { row[C_TIME], cls.getSimpleName(), row[0], row[C_STATUS], row[C_MS], row[C_NOTE] }, false);
            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
            if (isNew) {
                byte[] out = new byte[body.length + 3];
                out[0] = (byte) 0xEF; out[1] = (byte) 0xBB; out[2] = (byte) 0xBF;     // BOM UTF-8
                System.arraycopy(body, 0, out, 3, body.length);
                body = out;
            }
            Files.write(file, body, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[CsvLogger] Cannot write " + file + ": " + e.getMessage());
        }
    }

    private static void write(Class<?> cls) {
        Path file = fileOf(cls);
        try {
            Path dir = file.toAbsolutePath().getParent();
            if (dir != null) Files.createDirectories(dir);
            StringBuilder sb = new StringBuilder();
            appendRow(sb, HEADER);
            for (String[] r : FILES.get(cls.getName()).values()) appendRow(sb, r);
            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[body.length + 3];
            out[0] = (byte) 0xEF; out[1] = (byte) 0xBB; out[2] = (byte) 0xBF;     // BOM UTF-8
            System.arraycopy(body, 0, out, 3, body.length);
            Files.write(file, out);
        } catch (IOException e) {
            // Ví dụ file đang mở trong Excel: chỉ cảnh báo, không làm hỏng test.
            System.err.println("[CsvLogger] Cannot write " + file + ": " + e.getMessage());
        }
    }

    private static void appendRow(StringBuilder sb, String[] fields) { appendRow(sb, fields, true); }

    /** multiline = true: giữ xuống dòng thật trong ô (file Log); false: đổi xuống dòng thành " ⏎ " (file History). */
    private static void appendRow(StringBuilder sb, String[] fields, boolean multiline) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            String s = fields[i] == null ? "" : fields[i].replaceAll("\\R", multiline ? NL : SEP);
            sb.append('"').append(s.replace("\"", "\"\"")).append('"');
        }
        sb.append("\r\n");
    }
}
