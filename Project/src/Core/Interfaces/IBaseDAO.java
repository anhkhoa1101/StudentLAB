package Core.Interfaces;


import java.util.List;
import java.util.Optional;

public interface IBaseDAO<E> {
    List<E> readAll();
    boolean writeAll(List<E> list);
    boolean save();
    boolean add (E item);
    boolean update(E item);
    boolean delete (String id);
    Optional<E> findByID(String id);
}

