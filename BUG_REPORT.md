# 🐛 BUG REPORT & SECURITY AUDIT - BTLJAVA

## 🔴 LỖI NGHIÊM TRỌNG (CRITICAL)

### 1. **Mật khẩu lưu Plaintext (CVSS 7.5)**
- **File:** `EmployeeManager.java` (dòng 40, 57-67, 69-86)
- **Vấn đề:** Mật khẩu không mã hóa, lưu plaintext trong DB
- **Ảnh hưởng:** Nếu DB bị rò rỉ, tất cả mật khẩu lộ ra
- **Cách sửa:** Dùng BCrypt hoặc SHA-256 + salt
```java
// Cũ:
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

// Mới (cần thêm thư viện BCrypt):
import org.mindrot.jbcrypt.BCrypt;

// Khi đăng ký:
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

// Khi đăng nhập:
boolean isCorrect = BCrypt.checkpw(password, storedHash);
```

---

### 2. **Race Condition - Sinh Mã Nhân Viên (CVSS 5.3)**
- **File:** `EmployeeManager.java` (dòng 344-374)
- **Vấn đề:** Khi 2 luồng cùng sinh mã ngoài cùng lúc, có thể ghi trùng ID
- **Ảnh hưởng:** Dữ liệu bị trùng lặp, vi phạm constraint
- **Cách sửa:** Dùng transaction với LOCK hoặc UNIQUE constraint

```java
// Thay vì kiểm tra rồi insert, hãy:
// 1. Thêm UNIQUE constraint vào database:
// ALTER TABLE employees ADD UNIQUE(id);

// 2. Xử lý exception khi insert trùng:
private boolean checkIdExists(String id) {
    String sql = "SELECT 1 FROM employees WHERE id = ? FOR UPDATE";
    // FOR UPDATE sẽ lock row này
    ...
}
```

---

### 3. **NullPointerException - myProfile không kiểm tra**
- **File:** `EmployeeDashboardUI.java` (dòng 56, 136, 770-786)
- **Vấn đề:** `myProfile` có thể null nhưng không kiểm tra toàn bộ
```java
// Dòng 56:
myProfile = EmployeeManager.getInstance().getCurrentEmployeeProfile();
// ← myProfile có thể null tại đây!

// Dòng 770-786: Dùng myProfile.getNgaySinh() mà không kiểm tra null
String ngaySinh = myProfile.getNgaySinh() != null ? ... : "Chưa cập nhật";
// ← Nếu myProfile == null → NullPointerException!
```
- **Cách sửa:**
```java
if (myProfile == null) {
    JOptionPane.showMessageDialog(this, "Lỗi: Không thể tải hồ sơ!");
    new LoginUI();
    dispose();
    return;
}
```

---

### 4. **Exception Bị Bỏ Qua (CVSS 4.0)**
- **File:** Toàn bộ code, ví dụ: `EmployeeManager.java` dòng 163, 246, 370-372, 395, 430
- **Vấn đề:** Catch exception nhưng chỉ `e.printStackTrace()`, người dùng không biết
- **Ảnh hưởng:** Lỗi bị nuốt, khó debug, UX xấu
- **Cách sửa:**
```java
// Cũ:
catch (Exception e) { e.printStackTrace(); }

// Mới:
catch (SQLException sqlEx) {
    System.err.println("❌ Lỗi Database: " + sqlEx.getMessage());
    JOptionPane.showMessageDialog(this, 
        "Lỗi cơ sở dữ liệu: " + sqlEx.getMessage(), 
        "Lỗi", JOptionPane.ERROR_MESSAGE);
} catch (Exception e) {
    System.err.println("❌ Lỗi: " + e.getMessage());
    e.printStackTrace();
}
```

---

### 5. **Input Validation Yếu (CVSS 5.0)**
- **File:** `EmployeeManager.java` (dòng 88-125, 127-177)
- **Vấn đề:** Chỉ kiểm tra trống, không kiểm tra format email, độ mạnh mật khẩu
```java
// Cũ:
if (u.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
    return "...";
}

// Mới:
if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
    return "Email không hợp lệ!";
}
if (!phone.matches("^[0-9]{10,11}$")) {
    return "SĐT phải 10-11 chữ số!";
}
if (password.length() < 8 || !password.matches(".*[0-9].*") || !password.matches(".*[a-zA-Z].*")) {
    return "Mật khẩu: ≥8 ký tự, chữ + số + ký tự đặc biệt!";
}
```

---

## 🟡 CÁC LỖI TIỀM ẨN (HIGH)

### 6. **Logic Sai Khi Tính Phạt (dòng 793)**
```java
// Cũ:
if (lateMinutes > 5) penalty += (lateMinutes / 60.0) * hourlyRate;
// ← Nếu lateMinutes âm → phạt âm (sai!)

// Mới:
if (lateMinutes > 5) {
    penalty += (lateMinutes / 60.0) * hourlyRate;  // Phạt đi muộn
}
```

### 7. **Kiểm Tra Null Tại Dòng 542 (EmployeeDashboardUI)**
```java
// Cũ:
if (!shiftToday.startsWith("Ca 1") && !shiftToday.startsWith("Ca 2")) {
// ← Nếu shiftToday == null → crash!

// Mới:
if (shiftToday == null || (!shiftToday.startsWith("Ca 1") && !shiftToday.startsWith("Ca 2"))) {
```

### 8. **Memory Leak Timer (dòng 37, 1334, 1356, 1370, 1417)**
- **Vấn đề:** Timer không bị stop khi window đóng → memory leak
- **Cách sửa:** Hãy chắc chắn dispose() được gọi:
```java
@Override
public void dispose() {
    if (notifTimer != null && notifTimer.isRunning()) {
        notifTimer.stop();
    }
    super.dispose();
}
// ✔️ Đã làm tốt rồi, nhưng nên test kỹ
```

### 9. **Hiệu Năng: N+1 Query Problem**
- **File:** `SalaryCalculator.java` (dòng 26, 51, 57)
- **Vấn đề:** Tính lương 100 nhân viên = 300+ queries!
- **Cách sửa:** Batch query hoặc JOIN một lần

---

## 🟢 ĐIỂM MẠNH

✔️ Đã dùng PreparedStatement → tránh SQL Injection  
✔️ Đã xử lý transaction tốt (commit/rollback)  
✔️ Singleton pattern để quản lý kết nối  
✔️ UI đẹp và chuyên nghiệp  
✔️ Phân quyền ADMIN/EMPLOYEE tốt  

---

## ✅ CHECKLIST SỬA

- [ ] 1. Mã hóa mật khẩu bằng BCrypt
- [ ] 2. Thêm UNIQUE constraint cho ID nhân viên
- [ ] 3. Thêm kiểm tra null cho myProfile
- [ ] 4. Thay thế catch trống bằng thông báo lỗi UI
- [ ] 5. Thêm regex validate cho email/phone/mật khẩu
- [ ] 6. Kiểm tra lỗi tính phạt dương/âm
- [ ] 7. Kiểm tra null cho shiftToday
- [ ] 8. Tối ưu query database (batch/join)
- [ ] 9. Viết unit test cho các hàm tính toán
- [ ] 10. Thêm log lỗi chi tiết (sử dụng logging framework)

---

**Ngày báo cáo:** 2026-05-11  
**Mức độ nguy hiểm:** Trung bình (có lỗi bảo mật nhưng không quá tới)  
**Ưu tiên sửa:** 1 > 2 > 5 > 3 > 4
