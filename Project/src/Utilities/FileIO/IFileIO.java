package Utilities.FileIO;

import java.util.List;

public interface IFileIO<E> {
    List<E> readFromFile() throws Exception;
    boolean saveToFile(List<E> list) throws Exception;
}