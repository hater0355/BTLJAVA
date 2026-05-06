package BaiTapLon;

/**
 * Lớp này chịu TRÁCH NHIỆM DUY NHẤT cho việc tính lương.
 * Bằng cách tách logic này ra, chúng ta tránh lặp lại code (DRY principle)
 * và làm cho cả DashboardUI và EmployeeDashboardUI trở nên gọn gàng hơn.
 */
public class SalaryCalculator {

    private static final int STANDARD_WORK_DAYS = 22;

    /**
     * Tính toán lương cho một nhân viên trong một tháng cụ thể.
     * @param employee Đối tượng nhân viên cần tính lương.
     * @param month Tháng cần tính.
     * @param year Năm cần tính.
     * @param bonus Khoản thưởng/phụ cấp thêm.
     * @return Một đối tượng SalaryRecord chứa kết quả chi tiết.
     */
    public static SalaryRecord calculateSalary(Employee employee, int month, int year, double bonus) {
        if (employee == null) {
            throw new IllegalArgumentException("Đối tượng nhân viên không được rỗng.");
        }

        // 1. Lấy số ngày công từ CSDL
        int[] attendanceCounts = EmployeeManager.getInstance().getAttendanceCount(employee.getId(), month, year);
        int regularDays = attendanceCounts[0]; // Ngày làm T2-T6
        int overtimeDays = attendanceCounts[1]; // Ngày làm T7-CN

        // 2. Áp dụng quy tắc "làm bù"
        int finalRegularDays = regularDays;
        int finalOvertimeDays = overtimeDays;

        if (regularDays < STANDARD_WORK_DAYS) {
            int daysShort = STANDARD_WORK_DAYS - regularDays;
            if (overtimeDays <= daysShort) {
                // Nếu ngày làm thêm <= ngày thiếu -> Chuyển hết làm thêm sang làm bù
                finalRegularDays += overtimeDays;
                finalOvertimeDays = 0;
            } else {
                // Nếu ngày làm thêm > ngày thiếu -> Bù cho đủ ngày thường, còn lại tính tăng ca
                finalRegularDays = STANDARD_WORK_DAYS;
                finalOvertimeDays = overtimeDays - daysShort;
            }
        }

        // 3. Tính toán tiền lương
        double dailyRate = employee.getBaseSalary() / STANDARD_WORK_DAYS;
        
        // Truy xuất khoản phạt đi muộn / về sớm từ CSDL
        double penalty = EmployeeManager.getInstance().getAttendancePenalty(employee.getId(), month, year, dailyRate);
        
        double finalSalary = (dailyRate * finalRegularDays)      // Lương ngày thường
                           + ((dailyRate * 2) * finalOvertimeDays) // Lương tăng ca
                           + bonus                                 // Thưởng
                           - penalty;                              // Trừ tiền phạt
                           
        if (finalSalary < 0) finalSalary = 0; // Đảm bảo lương không bị trừ tới mức âm

        // 4. Trả về một đối tượng kết quả
        String monthYear = month + "/" + year;
        return new SalaryRecord(employee.getName(), monthYear, finalRegularDays, finalOvertimeDays, penalty, finalSalary);
    }
}