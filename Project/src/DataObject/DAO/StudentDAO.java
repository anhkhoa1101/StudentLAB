package DataObject.DAO;

import Core.Entities.Student;
import Core.Interfaces.IStudentDAO;
import Utilities.FileIO.IFileIO;
import Utilities.FileIO.StudentFileIO;

public class StudentDAO extends BaseDAO<Student> implements IStudentDAO {

    private static final String DEFAULT_PATH = "src/DataObject/data/Students.txt";

    public StudentDAO() {
        this(new StudentFileIO(DEFAULT_PATH));
    }

    public StudentDAO(IFileIO<Student> fileIO) {
        super(fileIO, "student");
    }

    @Override
    protected String keyOf(Student s) { return s.getId(); }
}