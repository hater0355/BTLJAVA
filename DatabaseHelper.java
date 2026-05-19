package BaiTapLon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

    // =========================================================
    // HÀM TỰ ĐỘNG SINH DATABASE VÀ CÁC BẢNG LẦN ĐẦU CHẠY
    // =========================================================
public class DatabaseHelper {
    
    // link 1: Dùng để kết nối lúc đầu khi máy người dùng chưa hề có Database
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    
    // link 2: Dùng để kết nối làm việc bình thường sau khi đã có Database "quanlyluong"
    private static final String URL = "jdbc:mysql://localhost:3306/quanlyluong?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; 
    private static final String PASS = "";     

    // Hàm kết nối thông thường cho toàn bộ phần mềm
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    
    public static void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connBase = DriverManager.getConnection(BASE_URL, USER, PASS);
                 Statement stmtBase = connBase.createStatement()) {
                
                stmtBase.execute("CREATE DATABASE IF NOT EXISTS quanlyluong CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                System.out.println("✅ Đã kiểm tra/tạo thành công Database 'quanlyluong'");
            }

            try (Connection conn = getConnection(); 
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("⏳ Đang kiểm tra và tự động xây dựng các bảng dữ liệu...");

                stmt.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) PRIMARY KEY, password VARCHAR(255), role VARCHAR(20), company_code VARCHAR(20), full_name VARCHAR(100), email VARCHAR(100), phone VARCHAR(20))");
                stmt.execute("CREATE TABLE IF NOT EXISTS employees (id VARCHAR(20) PRIMARY KEY, name VARCHAR(100), department VARCHAR(100), position VARCHAR(100), baseSalary DOUBLE, account_username VARCHAR(50), login_username VARCHAR(50), status VARCHAR(20) DEFAULT 'PENDING', ngay_sinh DATE, gia_dinh VARCHAR(255), lien_lac_khan VARCHAR(50), ngay_vao_lam DATE DEFAULT (CURRENT_DATE), nguoi_phu_thuoc INT DEFAULT 0)");
                stmt.execute("CREATE TABLE IF NOT EXISTS departments (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), account_username VARCHAR(50))");
                stmt.execute("CREATE TABLE IF NOT EXISTS notifications (id INT AUTO_INCREMENT PRIMARY KEY, account_username VARCHAR(50), message TEXT, created_at DATETIME)");
                stmt.execute("CREATE TABLE IF NOT EXISTS rewards_penalties (id INT AUTO_INCREMENT PRIMARY KEY, employee_id VARCHAR(50), month INT, year INT, amount DOUBLE, reason TEXT, recorded_by VARCHAR(50))");
                stmt.execute("CREATE TABLE IF NOT EXISTS salary_history (id INT AUTO_INCREMENT PRIMARY KEY, employee_id VARCHAR(20), pay_month INT, pay_year INT, total_salary DOUBLE, calculated_at DATE)");
                stmt.execute("CREATE TABLE IF NOT EXISTS schedules (employee_id VARCHAR(20) NOT NULL, work_date DATE NOT NULL, shift VARCHAR(100), PRIMARY KEY (employee_id, work_date))");
                stmt.execute("CREATE TABLE IF NOT EXISTS timekeeping (employee_id VARCHAR(20) NOT NULL, work_date DATE NOT NULL, check_in TIME, check_out TIME, PRIMARY KEY (employee_id, work_date))");

                System.out.println("✅ Tự động xây dựng hoàn tất! Phần mềm sẵn sàng hoạt động.");
            }
            
        } catch (Exception e) {
            System.out.println("❌ LỖI KHỞI TẠO CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}