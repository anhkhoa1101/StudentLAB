package Support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sổ ghi Input / Output cho các test KHÔNG đi qua console (Mock test, test lớp nghiệp vụ...).
 * Test gọi:   TestLog.input("add(STU0001)");   TestLog.output("save() -> true");
 * CsvLogger sẽ đưa nội dung vào cột "Input thực tế" / "Output thực tế" của dòng TC đang chạy.
 * Với test console, ConsoleSim tự ghi, không cần gọi gì.
 */
public final class TestLog {

    private static final List<String> INPUT = new ArrayList<>();
    private static final List<String> OUTPUT = new ArrayList<>();

    private TestLog() {}

    public static synchronized void input(String text) { INPUT.add(text); }

    public static synchronized void output(String text) { OUTPUT.add(text); }

    static synchronized void reset() {
        INPUT.clear();
        OUTPUT.clear();
    }

    static synchronized List<String> inputs() { return Collections.unmodifiableList(new ArrayList<>(INPUT)); }

    static synchronized List<String> outputs() { return Collections.unmodifiableList(new ArrayList<>(OUTPUT)); }
}
