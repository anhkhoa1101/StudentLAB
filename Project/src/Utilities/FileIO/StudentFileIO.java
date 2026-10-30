package Utilities.FileIO;

import Core.Entities.Student;

public class StudentFileIO extends FileHelper<Student> {

    public StudentFileIO(String path) {
        super(path);
    }

    @Override
    protected Student parse(String line) {
        String[] p = line.split(",");
        if (p.length < 4) return null;
        return new Student(p[0].trim(), p[1].trim(), p[2].trim(),
                Double.parseDouble(p[3].trim()));
    }

    @Override
    protected String format(Student s) {
        return s.getId() + ", " + s.getName() + ", " + s.getMajor() + ", " + s.getGpa();
    }
}