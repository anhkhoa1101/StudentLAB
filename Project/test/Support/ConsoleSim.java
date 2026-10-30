package Support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Giả lập bàn phím + màn hình console cho test:
 *  - feed(...)     : đặt kịch bản "người dùng gõ" (mỗi phần tử = 1 dòng + Enter)
 *  - output()      : chữ CHƯƠNG TRÌNH in ra màn hình
 *  - typed()       : các dòng người dùng THỰC SỰ đã gõ (chương trình đã đọc)
 *  - transcript()  : màn hình đúng như người dùng thấy: prompt + phím đã gõ xen kẽ
 *
 * Vòng đời do CsvLogger quản lý (begin() trước mỗi test, end() sau mỗi test).
 */
public final class ConsoleSim {

    private static final Deque<String> SCRIPT = new ArrayDeque<>();
    private static final List<String> TYPED = new ArrayList<>();
    private static final StringBuilder TRANSCRIPT = new StringBuilder();
    private static int mark;                       // vị trí trong output() đã đưa vào transcript
    private static boolean installed;
    private static PrintStream realOut;
    private static ByteArrayOutputStream captured;

    private ConsoleSim() {}

    /**
     * Thay System.in bằng bàn phím giả. Phải chạy TRƯỚC khi lớp Inputter được nạp lần đầu
     * (Inputter giữ System.in trong 1 Scanner static), nên CsvLogger gọi nó ở đầu mỗi test.
     */
    public static synchronized void install() {
        if (installed) return;
        System.setIn(new ScriptedInput());
        installed = true;
    }

    static synchronized void begin() {
        install();
        end();
        SCRIPT.clear();
        TYPED.clear();
        TRANSCRIPT.setLength(0);
        mark = 0;
        realOut = System.out;
        captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Trả System.out về như cũ. Dữ liệu đã ghi vẫn đọc được cho đến lần begin() kế tiếp. */
    static synchronized void end() {
        if (realOut != null) {
            System.setOut(realOut);
            realOut = null;
        }
    }

    /** Đặt kịch bản gõ phím. Mỗi chuỗi = 1 dòng; chuỗi "" = chỉ nhấn Enter. */
    public static synchronized void feed(String... lines) {
        SCRIPT.clear();
        for (String l : lines) SCRIPT.addLast(l);
    }

    public static synchronized String output() {
        if (captured == null) return "";
        try {
            return captured.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static synchronized List<String> typed() {
        return new ArrayList<>(TYPED);
    }

    public static synchronized String transcript() {
        String out = output();
        return TRANSCRIPT + out.substring(Math.min(mark, out.length()));
    }

    /** Số dòng đã đặt trong kịch bản nhưng chương trình không đọc tới. */
    public static synchronized int unusedInput() {
        return SCRIPT.size();
    }

    public static List<String> lines(String text) {
        List<String> r = new ArrayList<>();
        for (String s : text.split("\\R")) r.add(s);
        return r;
    }

    public static int count(String text, String part) {
        int n = 0, from = 0;
        while ((from = text.indexOf(part, from)) >= 0) { n++; from += part.length(); }
        return n;
    }

    /** Phát từng dòng một, giống người dùng gõ phím thật. */
    private static final class ScriptedInput extends InputStream {
        private byte[] cur = new byte[0];
        private int pos = 0;

        private void refill() {
            String line;
            synchronized (ConsoleSim.class) {
                line = SCRIPT.pollFirst();
                if (line == null) {
                    // Ném lỗi thay vì trả -1 để Scanner không bị "đóng" vĩnh viễn
                    throw new IllegalStateException(
                            "Test het du lieu nhap: chuong trinh hoi nhieu hon so dong test da cung cap.");
                }
                TYPED.add(line);
                String out = output();
                TRANSCRIPT.append(out, Math.min(mark, out.length()), out.length()).append(line).append('\n');
                mark = out.length();
            }
            cur = (line + "\n").getBytes(StandardCharsets.UTF_8);
            pos = 0;
        }

        @Override public int read() throws IOException {
            if (pos >= cur.length) refill();
            return cur[pos++] & 0xFF;
        }

        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) return 0;
            if (pos >= cur.length) refill();
            int n = Math.min(len, cur.length - pos);
            System.arraycopy(cur, pos, b, off, n);
            pos += n;
            return n;
        }

        @Override public int available() { return cur.length - pos; }
    }
}
