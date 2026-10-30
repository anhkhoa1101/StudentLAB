package DataObject.DAO;

import Core.Interfaces.IBaseDAO;
import Utilities.FileIO.IFileIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseDAO<E> implements IBaseDAO<E> {

    private final IFileIO<E> fileIO;
    private final String label;
    protected final List<E> items = new ArrayList<>();

    protected BaseDAO(IFileIO<E> fileIO, String label) {
        this.fileIO = fileIO;
        this.label = label;
        load();
    }

    /** Khóa duy nhất của phần tử (studentId / courseId). */
    protected abstract String keyOf(E item);

    private void load() {
        try {
            for (E e : fileIO.readFromFile()) {
                if (indexOf(keyOf(e)) >= 0) {
                    System.out.println("Skip duplicate " + label + " ID: " + keyOf(e));
                    continue;
                }
                items.add(e);
            }
        } catch (Exception ex) {
            System.out.println("Cannot read " + label + "s: " + ex.getMessage());
        }
    }

    private int indexOf(String id) {
        if (id == null) return -1;
        String key = id.trim();
        for (int i = 0; i < items.size(); i++) {
            if (keyOf(items.get(i)).equalsIgnoreCase(key)) return i;
        }
        return -1;
    }

    @Override
    public List<E> readAll() { return new ArrayList<>(items); }

    @Override
    public boolean writeAll(List<E> list) {
        if (list == null) return false;
        items.clear();
        items.addAll(list);
        return save();
    }

    @Override
    public boolean save() {
        try {
            return fileIO.saveToFile(items);
        } catch (Exception e) {
            System.out.println("Cannot save " + label + "s: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean add(E item) {
        if (item == null || indexOf(keyOf(item)) >= 0) return false;
        return items.add(item);
    }

    @Override
    public boolean update(E item) {
        if (item == null) return false;
        int i = indexOf(keyOf(item));        if (i < 0) return false;
        items.set(i, item);
        return true;
    }

    @Override
    public boolean delete(String id) {
        int i = indexOf(id);
        if (i < 0) return false;
        items.remove(i);
        return true;
    }

    @Override
    public Optional<E> findByID(String id) {
        int i = indexOf(id);
        return i < 0 ? Optional.empty() : Optional.of(items.get(i));
    }
}