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

    // Constructor: Nạp Driver
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
    // 1. LẤY DANH SÁCH NHÂN VIÊN ĐÃ ĐƯỢC DUYỆT (APPROVED)
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
                    list.add(emp);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 2. THÊM NHÂN VIÊN (Được gọi từ file Excel)
    // =========================================================
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

    // =========================================================
    // 3. CẬP NHẬT LƯƠNG NHÂN VIÊN
    // =========================================================
    public void updateSalary(String id, double newSalary) {
        String sql = "UPDATE employees SET baseSalary = ? WHERE id = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setDouble(1, newSalary);
            pstmt.setString(2, id);
            pstmt.setString(3, currentUsername); 
            pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    // ĐÃ THÊM: Sếp cập nhật Phòng ban & Chức vụ
    public void updateEmployeeDeptPos(String id, String dep, String pos) {
        String sql = "UPDATE employees SET department = ?, position = ? WHERE id = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dep); 
            pstmt.setString(2, pos); 
            pstmt.setString(3, id); 
            pstmt.setString(4, currentUsername); 
            pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    // =========================================================
    // 4. XÓA NHÂN VIÊN (SA THẢI)
    // =========================================================
    public void deleteEmployee(String id) {
        String sql = "DELETE FROM employees WHERE id = ? AND account_username = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, currentUsername); 
            pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    // =========================================================
    // TÍNH NĂNG ĐẶC BIỆT: TỰ ĐỘNG SINH MÃ VÀ NHẬP EXCEL
    // =========================================================
    public String generateEmployeeId() {
        int year = LocalDate.now().getYear() % 100; 
        String prefix = String.format("%02d", year);
        
        String newId;
        boolean exists = true;
        Random rand = new Random();
        
        do {
            String randomSuffix = String.format("%03d", rand.nextInt(999) + 1);
            newId = prefix + randomSuffix;
            exists = checkIdExists(newId);
        } while (exists);
        
        return newId;
    }

    private boolean checkIdExists(String id) {
        String sql = "SELECT 1 FROM employees WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeQuery().next();
        } catch (Exception e) { return true; } 
    }

    public void importEmployeeFromExcel(String name, String dep, String pos, double salary) {
        String empId = generateEmployeeId();
        
        String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, '123', 'EMPLOYEE')";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertUserSql)) {
            pstmt.setString(1, empId);
            pstmt.executeUpdate();
        } catch(Exception e) {}

        Employee newEmp = new Employee(empId, name, dep, pos, salary);
        addEmployee(newEmp);
    }

    // =========================================================
    // 5. HỆ THỐNG CHẤM CÔNG MỚI (LƯU THEO GIỜ THỰC TẾ)
    // =========================================================
    public void checkIn(String empId, LocalDate date, LocalTime time) {
        String sql = "INSERT INTO timekeeping (employee_id, work_date, check_in) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE check_in = VALUES(check_in)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setTime(3, java.sql.Time.valueOf(time)); 
            pstmt.executeUpdate();
        } catch (Exception e) {
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
        } catch (Exception e) {
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
                
                // ĐÃ THÊM: TỰ ĐỘNG CHECK-OUT LÚC 23:59 NẾU QUÊN CỦA NGÀY HÔM TRƯỚC
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
        } catch (Exception e) { }
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

    public void saveAttendance(String empId, LocalDate date, boolean isPresent) {}
    public boolean checkAttendance(String empId, LocalDate date) { return false; }

    // =========================================================
    // 6. QUẢN LÝ PHÒNG BAN (BẢNG departments)
    // =========================================================
    public void addDepartment(String name) {
        String sql = "INSERT INTO departments (name, account_username) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, name);
            pstmt.setString(2, currentUsername);
            pstmt.executeUpdate();
        } catch (Exception e) { }
    }

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

   // =========================================================
    // 7. XỬ LÝ TÀI KHOẢN (ĐĂNG NHẬP / ĐĂNG KÝ / ĐĂNG XUẤT)
    // =========================================================
    private String currentUsername = null;
    private String currentUserRole = null; 
    
    public String getCurrentUsername() { return currentUsername; }
    public String getCurrentUserRole() { return currentUserRole; }
    
    public void logoutUser() { 
        currentUsername = null; 
        currentUserRole = null; 
    }

    // ĐÃ SỬA: Đăng ký Quản lý (Thêm Tên, Email, Số điện thoại)
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
            
            String compCode = "COMP" + (1000 + new Random().nextInt(9000));
            
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); 
            insertStmt.setString(3, compCode);
            insertStmt.setString(4, fullName);
            insertStmt.setString(5, email);
            insertStmt.setString(6, phone);
            insertStmt.executeUpdate();
            return "SUCCESS";
        } catch (Exception e) { return "Lỗi: " + e.getMessage(); }
    }

    // ĐÃ SỬA: Đăng ký Nhân viên (Thêm Email, Số điện thoại)
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

            return "SUCCESS";
        } catch (Exception e) { return "Lỗi hệ thống: " + e.getMessage(); }
    }

    public String authenticateUser(String username, String password) {
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
        } catch (Exception e) { }
        return null; 
    }
    
    public void changePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    // ĐÃ THÊM: Tính năng Reset Mật Khẩu (Quên mật khẩu)
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
    
    public String getMyCompanyCode() {
        String sql = "SELECT company_code FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("company_code");
        } catch (Exception e) {}
        return "N/A";
    }

    // =========================================================
    // 8. CÁC HÀM DÀNH RIÊNG CHO CỔNG NHÂN VIÊN (EMPLOYEE)
    // =========================================================
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

    public Employee getCurrentEmployeeProfile() {
        String sql = "SELECT * FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Employee(rs.getString("id"), rs.getString("name"), rs.getString("department"), rs.getString("position"), rs.getDouble("baseSalary"));
            }
        } catch (Exception e) {}
        return null;
    }

    public String getCurrentEmployeeStatus() {
        String sql = "SELECT status FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (Exception e) {}
        return "NO_JOB"; 
    }

    // =========================================================
    // 9. QUẢN LÝ THÔNG BÁO VÀ LỊCH LÀM VIỆC (DỮ LIỆU THỰC TỪ DB)
    // =========================================================
    public void sendNotification(String message) {
        String sql = "INSERT INTO notifications (account_username, message, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); pstmt.setString(2, message); pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now())); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public List<String[]> getNotifications(String adminUsername) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT message, created_at FROM notifications WHERE account_username = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername); ResultSet rs = pstmt.executeQuery();
            while(rs.next()){ list.add(new String[]{rs.getDate("created_at").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), rs.getString("message")}); }
        } catch (Exception e) {}
        return list;
    }

    public String getMyAdminUsername() {
        String sql = "SELECT account_username FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("account_username");
        } catch (Exception e) {}
        return null;
    }

    public void saveSchedule(String empId, LocalDate date, String shift) {
        String sql = "INSERT INTO schedules (employee_id, work_date, shift) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE shift = VALUES(shift)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setDate(2, java.sql.Date.valueOf(date)); pstmt.setString(3, shift); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public String getSchedule(String empId, LocalDate date) {
        String sql = "SELECT shift FROM schedules WHERE employee_id = ? AND work_date = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setDate(2, java.sql.Date.valueOf(date)); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("shift");
        } catch (Exception e) {}
        return "Chưa đăng ký";
    }

    // =========================================================
    // 10. BÁO CÁO NGHỈ PHÉP VÀ VẮNG MẶT CỦA SẾP
    // =========================================================
    public void reviewLeaveRequest(String empId, LocalDate date, boolean isApproved) {
        String currentShift = getSchedule(empId, date);
        if (currentShift != null && (currentShift.startsWith("Chờ duyệt nghỉ: ") || currentShift.startsWith("Xin nghỉ: "))) {
            String reason = currentShift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
            String newShift = isApproved ? "Đã duyệt nghỉ: " + reason : "Từ chối nghỉ: " + reason;
            saveSchedule(empId, date, newShift);
        }
    }

    public List<String[]> getDailyAbsenceReport(LocalDate date) {
        List<String[]> report = new ArrayList<>();
        List<Employee> emps = getAllEmployees();
        for (Employee e : emps) {
            String shift = getSchedule(e.getId(), date);
            String[] time = getAttendanceRecord(e.getId(), date);
            if (shift.startsWith("Chờ duyệt nghỉ") || shift.startsWith("Xin nghỉ")) {
                String reason = shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
                report.add(new String[]{e.getId(), e.getName(), "Chờ duyệt", reason});
            } else if (shift.startsWith("Đã duyệt nghỉ")) {
                report.add(new String[]{e.getId(), e.getName(), "Nghỉ CÓ phép", shift.replace("Đã duyệt nghỉ: ", "")});
            } else if (shift.startsWith("Từ chối nghỉ")) {
                report.add(new String[]{e.getId(), e.getName(), "Bị từ chối nghỉ", shift.replace("Từ chối nghỉ: ", "")});
            } else if (!shift.equals("Nghỉ") && !shift.equals("Chưa đăng ký")) {
                if (time[0] == null && !date.isAfter(LocalDate.now())) { report.add(new String[]{e.getId(), e.getName(), "Nghỉ KHÔNG phép", "Bỏ ca: " + shift}); }
            }
        }
        return report;
    }
}
