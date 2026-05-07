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
        double[] penaltyData = EmployeeManager.getInstance().getAttendancePenalty(employee.getId(), month, year, dailyRate);
        double penalty = penaltyData[0];
        int forgotCheckOutCount = (int) penaltyData[1];
        double totalWorkingHours = penaltyData[2] / 60.0;
        
        // Đồng bộ khoản Thưởng/Phạt từ Trưởng phòng
        double deptRewardPenalty = EmployeeManager.getInstance().getTotalRewardPenalty(employee.getId(), month, year);
        
        double grossSalary = (dailyRate * finalRegularDays)      // Lương ngày thường
                           + ((dailyRate * 2) * finalOvertimeDays) // Lương tăng ca
                           + bonus                                 // Thưởng thêm thủ công từ Giám đốc
                           + deptRewardPenalty;                    // Thưởng/Phạt đồng bộ từ Trưởng phòng
                           
        // Tính Bảo hiểm xã hội (Mặc định 10.5% trên Lương cơ bản)
        double insurance = employee.getBaseSalary() * 0.105;

        // Tính thuế TNCN
        double tax = calculateTNCN(grossSalary, insurance, employee.getNguoiPhuThuoc());
        
        double finalSalary = grossSalary - penalty - insurance - tax; // Lương thực lĩnh = Tổng thu nhập - Phạt - BHXH - Thuế
                           
        if (finalSalary < 0) finalSalary = 0; // Đảm bảo lương không bị trừ tới mức âm

        // 4. Trả về một đối tượng kết quả
        String monthYear = month + "/" + year;
        return new SalaryRecord(employee.getName(), monthYear, finalRegularDays, finalOvertimeDays, penalty, insurance, tax, finalSalary, forgotCheckOutCount, totalWorkingHours);
    }
    
    /**
     * Tính thuế TNCN theo biểu thuế lũy tiến từng phần
     */
    private static double calculateTNCN(double income, double insurance, int dependents) {
        double personalDeduction = 11000000; // Giảm trừ bản thân (11.000.000 VNĐ)
        double dependentDeduction = 4400000 * dependents; // Giảm trừ người phụ thuộc (4.400.000 VNĐ/người)
        double taxableIncome = income - personalDeduction - dependentDeduction - insurance; // Được trừ BHXH và giảm trừ gia cảnh

        if (taxableIncome <= 0) return 0;

        double tax = 0;
        if (taxableIncome <= 5000000) {
            tax = taxableIncome * 0.05;
        } else if (taxableIncome <= 10000000) {
            tax = taxableIncome * 0.10 - 250000;
        } else if (taxableIncome <= 18000000) {
            tax = taxableIncome * 0.15 - 750000;
        } else if (taxableIncome <= 32000000) {
            tax = taxableIncome * 0.20 - 1650000;
        } else if (taxableIncome <= 52000000) {
            tax = taxableIncome * 0.25 - 3250000;
        } else if (taxableIncome <= 80000000) {
            tax = taxableIncome * 0.30 - 5850000;
        } else {
            tax = taxableIncome * 0.35 - 9850000;
        }
        return tax;
    }
}