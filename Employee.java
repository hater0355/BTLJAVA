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

    // THÊM MỚI: Hàm tạo mặc định (Cần thiết khi khởi tạo đối tượng rỗng từ CSDL)
    public Employee() {
    }

    // Hàm tạo có tham số (Dùng khi muốn khởi tạo nhanh đối tượng có sẵn dữ liệu cơ bản)
    public Employee(String id, String name, String department, String position, double baseSalary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.position = position;
        this.baseSalary = baseSalary;
    }

    // --- CÁC GETTER VÀ SETTER CHO THUỘC TÍNH CƠ BẢN ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; } // Thêm setter

    public String getName() { return name; }
    public void setName(String name) { this.name = name; } // Thêm setter

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; } // Thêm setter

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; } // Thêm setter

    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; } // Thêm setter

    // --- CÁC GETTER VÀ SETTER CHO THÔNG TIN CÁ NHÂN ---
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGiaDinh() { return giaDinh; }
    public void setGiaDinh(String giaDinh) { this.giaDinh = giaDinh; }

    public String getLienLacKhan() { return lienLacKhan; }
    public void setLienLacKhan(String lienLacKhan) { this.lienLacKhan = lienLacKhan; }

    public LocalDate getNgayVaoLam() { return ngayVaoLam; }
    public void setNgayVaoLam(LocalDate ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    // --- CÁC HÀM TÍNH TOÁN TỰ ĐỘNG ---
    
    // Tính tuổi
    public int getTuoi() {
        if (ngaySinh != null) {
            return Period.between(ngaySinh, LocalDate.now()).getYears();
        }
        return 0;
    }

    // Tính thâm niên (Số năm làm việc)
    public int getThamNien() {
        if (ngayVaoLam != null) {
            return Period.between(ngayVaoLam, LocalDate.now()).getYears();
        }
        return 0;
    }
}