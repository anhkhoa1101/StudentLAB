package Core.Entities;



public class Student {

    private String id;      // STU0001
    private String name;
    private String major;
    private double gpa;

    public Student() {
    }

    public Student(String id, String name, String major, double gpa) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.gpa = gpa;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    // 1 dòng trong Students.txt: id, name, major, gpa
    public String toDataString() {
        return id + ", " + name + ", " + major + ", " + gpa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;

        Student other = (Student) o;

        return id == null ? other.id == null : id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
}



    @Override
    public String toString() {
        return String.format("%-8s | %-25s | %-25s | %.1f", id, name, major, gpa);
    }
}