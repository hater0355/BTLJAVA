package BaiTapLon;

import java.time.LocalDate;
import java.time.Period;

public class Employee {
    private String id;
    private String name;
    private String department; 
    private String position;
    private double baseSalary;
    
    private LocalDate ngaySinh;
    private String giaDinh;
    private String lienLacKhan;
    private LocalDate ngayVaoLam;
    private int nguoiPhuThuoc;

    public Employee() {
    }

    public Employee(String id, String name, String department, String position, double baseSalary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.position = position;
        this.baseSalary = baseSalary;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGiaDinh() { return giaDinh; }
    public void setGiaDinh(String giaDinh) { this.giaDinh = giaDinh; }

    public String getLienLacKhan() { return lienLacKhan; }
    public void setLienLacKhan(String lienLacKhan) { this.lienLacKhan = lienLacKhan; }

    public LocalDate getNgayVaoLam() { return ngayVaoLam; }
    public void setNgayVaoLam(LocalDate ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    public int getNguoiPhuThuoc() { return nguoiPhuThuoc; }
    public void setNguoiPhuThuoc(int nguoiPhuThuoc) { this.nguoiPhuThuoc = nguoiPhuThuoc; }

    
    public int getTuoi() {
        if (ngaySinh != null) {
            return Period.between(ngaySinh, LocalDate.now()).getYears();
        }
        return 0;
    }

    public int getThamNien() {
        if (ngayVaoLam != null) {
            return Period.between(ngayVaoLam, LocalDate.now()).getYears();
        }
        return 0;
    }
}