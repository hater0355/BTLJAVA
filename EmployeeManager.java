package BaiTapLon;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime; 
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Random; 
import java.time.format.DateTimeFormatter;

public class EmployeeManager {
    private static EmployeeManager instance;
    private String currentUsername = null;
    private String currentUserRole = null; 

    // =========================================================
    // 1. SINGLETON & KHỞI TẠO CƠ SỞ DỮ LIỆU
    // =========================================================
    private EmployeeManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL đã được nạp thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Lỗi: Không tìm thấy Driver MySQL (.jar).");
        }
    }

    public static EmployeeManager getInstance() {
        if (instance == null) {
            instance = new EmployeeManager();
        }
        return instance;
    }

    // =========================================================
    // 2. XÁC THỰC VÀ QUẢN LÝ TÀI KHOẢN (AUTH & ACCOUNT)
    // =========================================================
    public String authenticateUser(String username, String password) throws Exception {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentUsername = username; 
                currentUserRole = rs.getString("role");
                return currentUserRole; 
            }
        }
        return null; 
    }
    
    public void changePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean resetPassword(String username, String email, String newPassword) {
        String sqlCheck = "SELECT 1 FROM users WHERE username = ? AND email = ?";
        String sqlUpdate = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck);
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
            
            pstmtCheck.setString(1, username);
            pstmtCheck.setString(2, email);
            if (pstmtCheck.executeQuery().next()) {
                pstmtUpdate.setString(1, newPassword);
                pstmtUpdate.setString(2, username);
                pstmtUpdate.executeUpdate();
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String registerAdmin(String username, String password, String fullName, String email, String phone) {
        String checkSql = "SELECT username, email, phone FROM users WHERE username = ? OR email = ? OR phone = ?";
        String insertSql = "INSERT INTO users (username, password, role, company_code, full_name, email, phone) VALUES (?, ?, 'ADMIN', ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
             
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            checkStmt.setString(3, phone);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                if (username.equals(rs.getString("username"))) return "Tên tài khoản đã tồn tại!";
                if (email.equals(rs.getString("email"))) return "Email này đã được sử dụng!";
                if (phone.equals(rs.getString("phone"))) return "Số điện thoại này đã được sử dụng!";
            }

            conn.setAutoCommit(false);
            try {
                String compCode = "COMP" + (1000 + new Random().nextInt(9000));
                
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); 
                insertStmt.setString(3, compCode);
                insertStmt.setString(4, fullName);
                insertStmt.setString(5, email);
                insertStmt.setString(6, phone);
                insertStmt.executeUpdate();

                conn.commit();
                return "SUCCESS";
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) { e.printStackTrace(); return "Lỗi hệ thống: " + e.getMessage(); }
    }

    public String registerEmployee(String username, String password, String fullName, String companyCode, String email, String phone) {
        String findBossSql = "SELECT username FROM users WHERE company_code = ? AND role = 'ADMIN'";
        String checkUserSql = "SELECT username, email, phone FROM users WHERE username = ? OR email = ? OR phone = ?";
        String insertUserSql = "INSERT INTO users (username, password, role, full_name, email, phone) VALUES (?, ?, 'EMPLOYEE', ?, ?, ?)";
        String insertEmpSql = "INSERT INTO employees (id, name, account_username, login_username, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement findBossStmt = conn.prepareStatement(findBossSql);
             PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql);
             PreparedStatement insertUserStmt = conn.prepareStatement(insertUserSql);
             PreparedStatement insertEmpStmt = conn.prepareStatement(insertEmpSql)) {
            
            findBossStmt.setString(1, companyCode);
            ResultSet rsBoss = findBossStmt.executeQuery();
            if (!rsBoss.next()) return "Mã công ty không tồn tại!";
            String bossUsername = rsBoss.getString("username");

            checkUserStmt.setString(1, username);
            checkUserStmt.setString(2, email);
            checkUserStmt.setString(3, phone);
            ResultSet rsCheck = checkUserStmt.executeQuery();
            if (rsCheck.next()) {
                if (username.equals(rsCheck.getString("username"))) return "Tên tài khoản đã tồn tại!";
                if (email.equals(rsCheck.getString("email"))) return "Email này đã được sử dụng!";
                if (phone.equals(rsCheck.getString("phone"))) return "Số điện thoại này đã được sử dụng!";
            }

            conn.setAutoCommit(false);
            try {
                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password);
                insertUserStmt.setString(3, fullName);
                insertUserStmt.setString(4, email);
                insertUserStmt.setString(5, phone);
                insertUserStmt.executeUpdate();
    
                String randomId = generateEmployeeId();
                insertEmpStmt.setString(1, randomId);
                insertEmpStmt.setString(2, fullName);
                insertEmpStmt.setString(3, bossUsername); 
                insertEmpStmt.setString(4, username);     
                insertEmpStmt.executeUpdate();
    
                conn.commit();
                return "SUCCESS";
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) { return "Lỗi hệ thống: " + e.getMessage(); }
    }

    public void logoutUser() { 
        currentUsername = null; 
        currentUserRole = null; 
    }
    
    public String getCurrentUsername() { return currentUsername; }
    
    public String getCurrentUserRole() { return currentUserRole; }

    public String getMyAdminUsername() {
        String sql = "SELECT account_username FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("account_username");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMyCompanyCode() {
        String sql = "SELECT company_code FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("company_code");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    // =========================================================
    // 3. HỒ SƠ NHÂN VIÊN & TUYỂN DỤNG
    // =========================================================
    public Employee getCurrentEmployeeProfile() {
        String sql = "SELECT id FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String empId = rs.getString("id");
                return getEmployeeProfile(empId);
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi tải hồ sơ đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Employee getEmployeeProfile(String id) {
        Employee emp = null;
        String sql = "SELECT * FROM employees WHERE id = ?"; 
        try (Connection conn = DatabaseHelper.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                emp = new Employee();
                emp.setId(rs.getString("id"));
                emp.setName(rs.getString("name"));
                emp.setDepartment(rs.getString("department"));
                emp.setPosition(rs.getString("position"));
                emp.setBaseSalary(rs.getDouble("baseSalary")); 
                try { emp.setNguoiPhuThuoc(rs.getInt("nguoi_phu_thuoc")); } catch (Exception ignored) {}

                java.sql.Date sqlNgaySinh = rs.getDate("ngay_sinh");
                if (sqlNgaySinh != null) emp.setNgaySinh(sqlNgaySinh.toLocalDate()); 

                java.sql.Date sqlNgayVaoLam = rs.getDate("ngay_vao_lam");
                if (sqlNgayVaoLam != null) emp.setNgayVaoLam(sqlNgayVaoLam.toLocalDate());

                emp.setGiaDinh(rs.getString("gia_dinh"));
                emp.setLienLacKhan(rs.getString("lien_lac_khan"));
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy hồ sơ nhân viên: " + e.getMessage());
            e.printStackTrace(); 
        }
        return emp; 
    }

    public String getCurrentEmployeeStatus() {
        String sql = "SELECT status FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NO_JOB"; 
    }

    public String[] getEmployeeContactInfo(String empId) {
        String sql = "SELECT u.email, u.phone FROM users u JOIN employees e ON u.username = e.login_username WHERE e.id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                return new String[]{
                    (email != null && !email.isEmpty()) ? email : "Chưa cập nhật",
                    (phone != null && !phone.isEmpty()) ? phone : "Chưa cập nhật"
                };
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new String[]{"Chưa cập nhật", "Chưa cập nhật"};
    }

    public boolean updateFirstTimeProfile(String id, LocalDate dob, String relationship, String emergencyPhone, int dependents) {
        String sql = "UPDATE employees SET ngay_sinh = ?, gia_dinh = ?, lien_lac_khan = ?, nguoi_phu_thuoc = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(dob)); 
            pstmt.setString(2, relationship);
            pstmt.setString(3, emergencyPhone);
            pstmt.setInt(4, dependents);
            pstmt.setString(5, id); 
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Đã cập nhật thành công hồ sơ cho ID: " + id);
                return true;
            } else {
                System.out.println("⚠️ Không tìm thấy ID nhân viên này trong CSDL: " + id);
                return false;
            }
        } catch (SQLException sqlException) {
            System.out.println("=== ❌ LỖI CƠ SỞ DỮ LIỆU ===");
            System.out.println("Chi tiết lỗi: " + sqlException.getMessage());
            sqlException.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("=== ❌ LỖI HỆ THỐNG JAVA ===");
            e.printStackTrace();
            return false;
        }
    }

    public String applyNewJob(String fullName, String companyCode) {
        String findBossSql = "SELECT username FROM users WHERE company_code = ? AND role = 'ADMIN'";
        String insertEmpSql = "INSERT INTO employees (id, name, account_username, login_username, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement findBossStmt = conn.prepareStatement(findBossSql);
             PreparedStatement insertEmpStmt = conn.prepareStatement(insertEmpSql)) {
            findBossStmt.setString(1, companyCode);
            ResultSet rsBoss = findBossStmt.executeQuery();
            if (!rsBoss.next()) return "Mã công ty không tồn tại!";
            String bossUsername = rsBoss.getString("username");
            String newEmployeeId = generateEmployeeId();
            insertEmpStmt.setString(1, newEmployeeId);      
            insertEmpStmt.setString(2, fullName);           
            insertEmpStmt.setString(3, bossUsername);       
            insertEmpStmt.setString(4, currentUsername);    
            insertEmpStmt.executeUpdate();
            return "SUCCESS";
        } catch (Exception e) { return "Lỗi hệ thống: " + e.getMessage(); }
    }

    public String generateEmployeeId() {
        int year = LocalDate.now().getYear() % 100; 
        String prefix = String.format("%02d", year);
        
        String newId;
        boolean exists = true;
        Random rand = new Random();
        int attempts = 0;
        
        do {
            String randomSuffix = String.format("%03d", rand.nextInt(999) + 1);
            newId = prefix + randomSuffix;
            exists = checkIdExists(newId);
            attempts++;
            if (attempts > 900) { newId = prefix + String.format("%04d", rand.nextInt(9999) + 1); exists = checkIdExists(newId); }
        } while (exists);
        
        return newId;
    }

    private boolean checkIdExists(String id) {
        String sql = "SELECT 1 FROM employees WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        } 
    }

    // =========================================================
    // 4. QUẢN LÝ NHÂN SỰ (NHÂN VIÊN)
    // =========================================================
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE account_username = ? AND status = 'APPROVED'";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, currentUsername);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("department"), 
                        rs.getString("position"),
                        rs.getDouble("baseSalary")
                    );
                    try { emp.setNguoiPhuThuoc(rs.getInt("nguoi_phu_thuoc")); } catch (Exception ignored) {}
                    list.add(emp);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    public List<Employee> getColleagues(String department, String adminUsername) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE account_username = ? AND department = ? AND status = 'APPROVED'";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername);
            pstmt.setString(2, department);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getString("id"));
                    emp.setName(rs.getString("name"));
                    emp.setDepartment(rs.getString("department"));
                    emp.setPosition(rs.getString("position"));
                    java.sql.Date sqlNgaySinh = rs.getDate("ngay_sinh");
                    if (sqlNgaySinh != null) emp.setNgaySinh(sqlNgaySinh.toLocalDate()); 
                    java.sql.Date sqlNgayVaoLam = rs.getDate("ngay_vao_lam");
                    if (sqlNgayVaoLam != null) emp.setNgayVaoLam(sqlNgayVaoLam.toLocalDate());
                    emp.setGiaDinh(rs.getString("gia_dinh"));
                    emp.setLienLacKhan(rs.getString("lien_lac_khan"));
                    list.add(emp);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getCompanyBirthdaysToday(String adminUsername) {
        List<String> birthdayNames = new ArrayList<>();
        String sql = "SELECT name FROM employees WHERE account_username = ? AND status = 'APPROVED' AND MONTH(ngay_sinh) = MONTH(CURRENT_DATE()) AND DAY(ngay_sinh) = DAY(CURRENT_DATE())";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    birthdayNames.add(rs.getString("name"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return birthdayNames;
    }

    public void addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (id, name, department, position, baseSalary, account_username, login_username, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'APPROVED')";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, emp.getId());
            pstmt.setString(2, emp.getName());
            pstmt.setString(3, emp.getDepartment()); 
            pstmt.setString(4, emp.getPosition());
            pstmt.setDouble(5, emp.getBaseSalary());
            pstmt.setString(6, currentUsername); 
            pstmt.setString(7, emp.getId()); 

            pstmt.executeUpdate();
            System.out.println("✅ Đã thêm nhân viên: " + emp.getName());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm nhân viên: " + e.getMessage());
        }
    }

    public boolean importEmployeeFromExcel(String name, String dep, String pos, double salary, String email, String phone, String address, String dobStr) {
        String checkSql = "SELECT 1 FROM employees WHERE name = ? AND department = ? AND position = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dep);
            pstmt.setString(3, pos);
            pstmt.setString(4, currentUsername);
            if (pstmt.executeQuery().next()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String empId = generateEmployeeId();
        
        String insertUserSql = "INSERT INTO users (username, password, role, full_name, email, phone) VALUES (?, '123', 'EMPLOYEE', ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertUserSql)) {
            pstmt.setString(1, empId);
            pstmt.setString(2, name);
            pstmt.setString(3, email.isEmpty() ? null : email);
            pstmt.setString(4, phone.isEmpty() ? null : phone);
            pstmt.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }

        LocalDate dob = null;
        if (dobStr != null && !dobStr.isEmpty()) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy");
                if (dobStr.contains("-")) fmt = DateTimeFormatter.ofPattern("d-M-yyyy");
                dob = LocalDate.parse(dobStr, fmt);
            } catch (Exception ex) {}
        }
        
        Employee newEmp = new Employee(empId, name, dep, pos, salary);
        if (dob != null) newEmp.setNgaySinh(dob);
        
        // Để trống thông tin liên hệ khẩn cấp để bắt buộc nhân viên tự cập nhật khi đăng nhập lần đầu
        
        addEmployeeFull(newEmp);
        return true;
    }

    private void addEmployeeFull(Employee emp) {
        String sql = "INSERT INTO employees (id, name, department, position, baseSalary, account_username, login_username, status, ngay_sinh, gia_dinh, lien_lac_khan) VALUES (?, ?, ?, ?, ?, ?, ?, 'APPROVED', ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getId());
            pstmt.setString(2, emp.getName());
            pstmt.setString(3, emp.getDepartment());
            pstmt.setString(4, emp.getPosition());
            pstmt.setDouble(5, emp.getBaseSalary());
            pstmt.setString(6, currentUsername);
            pstmt.setString(7, emp.getId());
            if (emp.getNgaySinh() != null) pstmt.setDate(8, java.sql.Date.valueOf(emp.getNgaySinh()));
            else pstmt.setNull(8, java.sql.Types.DATE);
            pstmt.setString(9, emp.getGiaDinh());
            pstmt.setString(10, emp.getLienLacKhan());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm nhân viên từ Excel: " + e.getMessage());
        }
    }

    public void updateSalaryAndDependents(String id, double newSalary, int dependents) {
        String sql = "UPDATE employees SET baseSalary = ?, nguoi_phu_thuoc = ? WHERE id = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setDouble(1, newSalary);
            pstmt.setInt(2, dependents);
            pstmt.setString(3, id);
            pstmt.setString(4, currentUsername); 
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEmployeeDeptPos(String id, String dep, String pos) {
        String sql = "UPDATE employees SET department = ?, position = ? WHERE id = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dep); 
            pstmt.setString(2, pos); 
            pstmt.setString(3, id); 
            pstmt.setString(4, currentUsername); 
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteEmployee(String id) {
        String sqlSelect = "SELECT login_username FROM employees WHERE id = ? AND account_username = ?";
        String sqlDelEmp = "DELETE FROM employees WHERE id = ? AND account_username = ?";
        String sqlDelUser = "DELETE FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtSel = conn.prepareStatement(sqlSelect);
                 PreparedStatement pstmtEmp = conn.prepareStatement(sqlDelEmp);
                 PreparedStatement pstmtUser = conn.prepareStatement(sqlDelUser)) {
                 
                pstmtSel.setString(1, id);
                pstmtSel.setString(2, currentUsername);
                ResultSet rs = pstmtSel.executeQuery();
                String loginUser = null;
                if (rs.next()) loginUser = rs.getString("login_username");
                
                pstmtEmp.setString(1, id);
                pstmtEmp.setString(2, currentUsername);
                pstmtEmp.executeUpdate();
                
                if (loginUser != null && !loginUser.isEmpty()) {
                    pstmtUser.setString(1, loginUser);
                    pstmtUser.executeUpdate();
                }
                
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // 5. QUẢN LÝ PHÒNG BAN
    // =========================================================
    public List<String> getAllDepartments() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM departments WHERE account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }

        String sqlOld = "SELECT DISTINCT department FROM employees WHERE account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlOld)) {
             
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String d = rs.getString("department");
                if (d != null && !d.isEmpty() && !list.contains(d)) {
                    list.add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }

        if (list.isEmpty()) list.add("Chung"); 
        return list;
    }

    public void addDepartment(String name) {
        String sql = "INSERT INTO departments (name, account_username) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, name);
            pstmt.setString(2, currentUsername);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDepartmentName(String oldName, String newName) {
        String sqlDept = "UPDATE departments SET name = ? WHERE name = ? AND account_username = ?";
        String sqlEmp = "UPDATE employees SET department = ? WHERE department = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtDept = conn.prepareStatement(sqlDept);
                 PreparedStatement pstmtEmp = conn.prepareStatement(sqlEmp)) {
                 
                pstmtDept.setString(1, newName); pstmtDept.setString(2, oldName); pstmtDept.setString(3, currentUsername);
                pstmtDept.executeUpdate();

                pstmtEmp.setString(1, newName); pstmtEmp.setString(2, oldName); pstmtEmp.setString(3, currentUsername);
                pstmtEmp.executeUpdate();

                conn.commit();
            } catch (Exception ex) { conn.rollback(); ex.printStackTrace(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteDepartment(String deptName) {
        String sqlDept = "DELETE FROM departments WHERE name = ? AND account_username = ?";
        String sqlEmp = "UPDATE employees SET department = 'Chung' WHERE department = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtDept = conn.prepareStatement(sqlDept);
                 PreparedStatement pstmtEmp = conn.prepareStatement(sqlEmp)) {
                 
                pstmtDept.setString(1, deptName); pstmtDept.setString(2, currentUsername);
                pstmtDept.executeUpdate();

                pstmtEmp.setString(1, deptName); pstmtEmp.setString(2, currentUsername);
                pstmtEmp.executeUpdate();

                conn.commit();
            } catch (Exception ex) { conn.rollback(); ex.printStackTrace(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // 6. CHẤM CÔNG (TIMEKEEPING)
    // =========================================================
    public void checkIn(String empId, LocalDate date, LocalTime time) {
        String sql = "INSERT INTO timekeeping (employee_id, work_date, check_in) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE check_in = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, empId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setTime(3, java.sql.Time.valueOf(time)); 
            
            pstmt.setTime(4, java.sql.Time.valueOf(time)); 
            
            pstmt.executeUpdate();
            System.out.println("✅ Check-in thành công cho ID: " + empId);
            
        } catch (Exception e) {
            System.out.println("❌ LỖI KHI CHECK-IN: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    public void checkOut(String empId, LocalDate date, LocalTime time) {
        String sql = "UPDATE timekeeping SET check_out = ? WHERE employee_id = ? AND work_date = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             pstmt.setTime(1, java.sql.Time.valueOf(time)); 
             pstmt.setString(2, empId);
             pstmt.setDate(3, java.sql.Date.valueOf(date));
             pstmt.executeUpdate();
             
             System.out.println("✅ Check-out thành công cho ID: " + empId);
             
        } catch (Exception e) {
            System.out.println("❌ LỖI KHI CHECK-OUT: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    public String[] getAttendanceRecord(String empId, LocalDate date) {
        String sql = "SELECT check_in, check_out FROM timekeeping WHERE employee_id = ? AND work_date = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            pstmt.setString(1, empId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Time in = rs.getTime("check_in");
                Time out = rs.getTime("check_out");
                
                if (in != null && out == null && date.isBefore(LocalDate.now())) {
                    out = java.sql.Time.valueOf("23:59:00");
                    String updateSql = "UPDATE timekeeping SET check_out = ? WHERE employee_id = ? AND work_date = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setTime(1, out);
                        updateStmt.setString(2, empId);
                        updateStmt.setDate(3, java.sql.Date.valueOf(date));
                        updateStmt.executeUpdate();
                    } catch (Exception ignored) {}
                }

                String strIn = (in != null) ? in.toString().substring(0, 5) : null;   
                String strOut = (out != null) ? out.toString().substring(0, 5) : null;
                return new String[]{strIn, strOut};
            }
        } catch (Exception e) { 
            System.out.println("❌ LỖI LẤY BẢN GHI CHẤM CÔNG: " + e.getMessage());
        }
        return new String[]{null, null};
    }

    public int[] getAttendanceCount(String empId, int month, int year) {
        int[] counts = new int[]{0, 0}; 
        String sql = "SELECT work_date FROM timekeeping WHERE employee_id = ? AND MONTH(work_date) = ? AND YEAR(work_date) = ? AND check_in IS NOT NULL";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LocalDate localDate = rs.getDate("work_date").toLocalDate();
                DayOfWeek day = localDate.getDayOfWeek();
                if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) counts[1]++; 
                else counts[0]++; 
            }
        } catch (Exception e) { }
        return counts;
    }

    public double[] getAttendancePenalty(String empId, int month, int year, double dailyRate) {
        double penalty = 0;
        int forgotCount = 0;
        long totalWorkedMinutes = 0;
        double hourlyRate = dailyRate / 8.0;
        
        String sql = "SELECT t.work_date, t.check_in, t.check_out, s.shift FROM timekeeping t " +
                     "JOIN schedules s ON t.employee_id = s.employee_id AND t.work_date = s.work_date " +
                     "WHERE t.employee_id = ? AND MONTH(t.work_date) = ? AND YEAR(t.work_date) = ?";
                     
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setInt(2, month); pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Time sqlIn = rs.getTime("check_in"); Time sqlOut = rs.getTime("check_out"); String shift = rs.getString("shift");
                LocalDate workDate = rs.getDate("work_date").toLocalDate();
                if (sqlIn == null) continue;
                
                LocalTime inTime = sqlIn.toLocalTime();
                LocalTime expectedStart = null;
                if (shift != null && shift.startsWith("Ca 1")) expectedStart = LocalTime.of(8, 0);
                else if (shift != null && shift.startsWith("Ca 2")) expectedStart = LocalTime.of(12, 0);
                
                if (expectedStart != null) {
                    long lateMinutes = java.time.Duration.between(expectedStart, inTime).toMinutes();
                    if (lateMinutes > 5) penalty += (lateMinutes / 60.0) * hourlyRate;
                }
                
                if (sqlOut == null) {
                    if (workDate.isBefore(LocalDate.now())) {
                        penalty += 200000;
                        forgotCount++;
                    }
                } else {
                    LocalTime outTime = sqlOut.toLocalTime();
                    if (outTime.equals(LocalTime.of(23, 59, 0))) {
                        penalty += 200000;
                        forgotCount++;
                    } else {
                        long workedMinutes = java.time.Duration.between(inTime, outTime).toMinutes();
                        if (workedMinutes > 0) {
                            // Bổ sung: Nếu làm việc trên 5 tiếng (300 phút), hệ thống sẽ tự động 
                            // trừ đi 1 tiếng nghỉ trưa để ra số giờ làm việc thực tế.
                            if (workedMinutes >= 300) {
                                workedMinutes -= 60; 
                            }
                            if (workedMinutes < 480) {
                                long missingMinutes = 480 - workedMinutes;
                                penalty += (missingMinutes / 60.0) * hourlyRate;
                            }
                            totalWorkedMinutes += workedMinutes;
                        }
                    }
                }
            }
        } catch (Exception e) { System.out.println("❌ LỖI TÍNH TIỀN PHẠT: " + e.getMessage()); }
        return new double[]{penalty, forgotCount, totalWorkedMinutes};
    }

    public void saveAttendance(String empId, LocalDate date, boolean isPresent) {}
    
    public boolean checkAttendance(String empId, LocalDate date) { 
        String sql = "SELECT 1 FROM timekeeping WHERE employee_id = ? AND work_date = ?";
        try (Connection conn = DatabaseHelper.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            return pstmt.executeQuery().next();
        } catch (Exception e) { 
            System.out.println("❌ LỖI KIỂM TRA CHẤM CÔNG: " + e.getMessage());
            return false; 
        } 
    }

    // =========================================================
    // 7. QUẢN LÝ LỊCH LÀM VIỆC & NGHỈ PHÉP
    // =========================================================
    public void saveSchedule(String empId, LocalDate date, String shift) {
        String sql = "INSERT INTO schedules (employee_id, work_date, shift) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE shift = ?";
        try (Connection conn = DatabaseHelper.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, empId); 
            pstmt.setDate(2, java.sql.Date.valueOf(date)); 
            pstmt.setString(3, shift); 
            
            pstmt.setString(4, shift); 
            
            pstmt.executeUpdate();
            System.out.println("✅ Đã lưu lịch làm việc cho: " + empId + " ngày " + date + " | Ca: " + shift);
            
        } catch (Exception e) {
            System.out.println("❌ LỖI KHI LƯU LỊCH LÀM VIỆC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getSchedule(String empId, LocalDate date) {
        String sql = "SELECT shift FROM schedules WHERE employee_id = ? AND work_date = ?";
        try (Connection conn = DatabaseHelper.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, empId); 
            pstmt.setDate(2, java.sql.Date.valueOf(date)); 
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String shift = rs.getString("shift");
                if (shift == null || shift.trim().isEmpty()) {
                    return "Chưa đăng ký";
                }
                return shift;
            }
        } catch (Exception e) {
            System.out.println("❌ LỖI KHI TÌM LỊCH LÀM VIỆC: " + e.getMessage());
            e.printStackTrace();
        }
        return "Chưa đăng ký";
    }

    public void reviewLeaveRequest(String empId, LocalDate date, boolean isApproved) {
        String currentShift = getSchedule(empId, date);
        if (currentShift != null && (currentShift.startsWith("Chờ duyệt nghỉ: ") || currentShift.startsWith("Xin nghỉ: "))) {
            String reason = currentShift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
            String newShift = isApproved ? "Đã duyệt nghỉ: " + reason : "Từ chối nghỉ: " + reason;
            saveSchedule(empId, date, newShift);
        }
    }

    public int reviewAllPendingLeaves(String empId, boolean isApproved) {
        int count = 0;
        String sql = "SELECT work_date, shift FROM schedules WHERE employee_id = ? AND (shift LIKE 'Chờ duyệt nghỉ:%' OR shift LIKE 'Xin nghỉ:%')";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LocalDate date = rs.getDate("work_date").toLocalDate();
                String shift = rs.getString("shift");
                String reason = shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
                String newShift = isApproved ? "Đã duyệt nghỉ: " + reason : "Từ chối nghỉ: " + reason;
                saveSchedule(empId, date, newShift);
                count++;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    public List<String[]> getDailyAbsenceReport(LocalDate date) {
        List<String[]> report = new ArrayList<>();
        String sql = "SELECT e.id, e.name, s.shift, t.check_in FROM employees e " +
                     "LEFT JOIN schedules s ON e.id = s.employee_id AND s.work_date = ? " +
                     "LEFT JOIN timekeeping t ON e.id = t.employee_id AND t.work_date = ? " +
                     "WHERE e.account_username = ? AND e.status = 'APPROVED'";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setString(3, currentUsername);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String shift = rs.getString("shift");
                    Time checkIn = rs.getTime("check_in");

                    if (shift == null) {
                        shift = "Chưa đăng ký";
                    }

                    if (shift.startsWith("Chờ duyệt nghỉ") || shift.startsWith("Xin nghỉ")) {
                        String reason = shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
                        report.add(new String[]{id, name, "Chờ duyệt", reason});
                    } else if (shift.startsWith("Đã duyệt nghỉ")) {
                        report.add(new String[]{id, name, "Nghỉ CÓ phép", shift.replace("Đã duyệt nghỉ: ", "")});
                    } else if (shift.startsWith("Từ chối nghỉ")) {
                        report.add(new String[]{id, name, "Bị từ chối nghỉ", shift.replace("Từ chối nghỉ: ", "")});
                    } else if (!shift.equals("Nghỉ") && !shift.equals("Chưa đăng ký")) {
                        if (checkIn == null && !date.isAfter(LocalDate.now())) {
                            report.add(new String[]{id, name, "Nghỉ KHÔNG phép", "Bỏ ca: " + shift});
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return report;
    }

    // =========================================================
    // 8. QUẢN LÝ THƯỞNG PHẠT
    // =========================================================
    public void saveRewardPenalty(String empId, int month, int year, double amount, String reason, String recordedBy) {
        String sql = "INSERT INTO rewards_penalties (employee_id, month, year, amount, reason, recorded_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setInt(2, month); pstmt.setInt(3, year);
            pstmt.setDouble(4, amount); pstmt.setString(5, reason); pstmt.setString(6, recordedBy);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public double getTotalRewardPenalty(String empId, int month, int year) {
        String sql = "SELECT SUM(amount) as total FROM rewards_penalties WHERE employee_id = ? AND month = ? AND year = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setInt(2, month); pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<String[]> getDepartmentRewardPenaltyHistory(String department, int month, int year) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT e.name, r.amount, r.reason FROM rewards_penalties r JOIN employees e ON r.employee_id = e.id WHERE e.department = ? AND r.month = ? AND r.year = ? ORDER BY r.id DESC";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department); pstmt.setInt(2, month); pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double amt = rs.getDouble("amount");
                list.add(new String[]{ rs.getString("name"), amt >= 0 ? "Thưởng" : "Phạt", String.format("%,.0f VNĐ", amt), rs.getString("reason") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================
    // 9. THÔNG BÁO & GIAO VIỆC
    // =========================================================
    public void sendNotification(String message) {
        String sql = "INSERT INTO notifications (account_username, message, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); pstmt.setString(2, message); pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotificationAsAdmin(String adminUsername, String message) {
        String sql = "INSERT INTO notifications (account_username, message, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername); pstmt.setString(2, message); pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<String[]> getNotifications(String adminUsername) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT message, created_at FROM notifications WHERE account_username = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername); ResultSet rs = pstmt.executeQuery();
            while(rs.next()){ list.add(new String[]{rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), rs.getString("message")}); }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void markTaskCompleted(String originalMessage, String adminUsername) {
        String sql = "UPDATE notifications SET message = ? WHERE account_username = ? AND message = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, originalMessage + " [HOÀN THÀNH]");
            pstmt.setString(2, adminUsername);
            pstmt.setString(3, originalMessage);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteTask(String exactMessage, String adminUsername) {
        String sql = "DELETE FROM notifications WHERE account_username = ? AND message = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername);
            pstmt.setString(2, exactMessage);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}