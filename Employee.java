package BaiTapLon;

public class Employee {
    private String id;
    private String name;
    private String department; // Thuộc tính mới: Phòng ban
    private String position;
    private double baseSalary;

    public Employee(String id, String name, String department, String position, double baseSalary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.position = position;
        this.baseSalary = baseSalary;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; } // Getter mới
    public String getPosition() { return position; }
    public double getBaseSalary() { return baseSalary; }
}