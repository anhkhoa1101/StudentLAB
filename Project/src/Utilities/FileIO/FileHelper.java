package Utilities.FileIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class FileHelper<E> implements IFileIO<E> {

    private final String path;

    protected FileHelper(String path) {
        this.path = path;
    }

    /** 1 dòng text -> E. Trả về null hoặc ném exception nếu dòng sai định dạng. */
    protected abstract E parse(String line) throws Exception;

    /** E -> 1 dòng text. */
    protected abstract String format(E item);

    @Override
    public List<E> readFromFile() throws Exception {
        List<E> list = new ArrayList<>();
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            return list; // chưa có file -> danh sách rỗng
        }

        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (lineNo == 1 && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1); // bỏ BOM
                }
                if (line.trim().isEmpty()) continue;

                try {
                    E item = parse(line);
                    if (item != null) list.add(item);
                    else System.out.println("Skip line " + lineNo + " (invalid format): " + line);
                } catch (Exception e) {
                    System.out.println("Skip line " + lineNo + " (" + e.getMessage() + "): " + line);
                }
            }
        }
        return list;
    }

    @Override
    public boolean saveToFile(List<E> list) throws Exception {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
            for (E item : list) {
                bw.write(format(item));
                bw.newLine();
            }
        }
        return true;
    }
}