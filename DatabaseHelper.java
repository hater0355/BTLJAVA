package BaiTapLon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/quanlyluong?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; // Mặc định của XAMPP là root
    private static final String PASS = "";     // Mặc định của XAMPP là để trống

    public static Connection getConnection() throws Exception {
        // Nạp Driver MySQL
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Tạo bảng nhân viên nếu chưa có
            String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                         "id VARCHAR(50) PRIMARY KEY, " +
                         "name VARCHAR(255), " +
                         "position VARCHAR(100), " +
                         "baseSalary DOUBLE)";
            stmt.execute(sql);
        } catch (Exception e) {
            System.out.println("Lỗi khởi tạo DB: " + e.getMessage());
        }
    }
    
}
