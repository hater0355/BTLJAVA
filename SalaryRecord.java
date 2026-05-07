package BaiTapLon;

// A simple data class (POJO) to hold the results of a salary calculation.
public class SalaryRecord {
    private final String employeeName;
    private final String monthYear;
    private final int finalRegularDays;
    private final int finalOvertimeDays;
    private final double penalty;
    private final double insurance;
    private final double tax;
    private final double finalSalary;
    private final int forgotCheckOutCount;
    private final double totalWorkingHours;

    public SalaryRecord(String employeeName, String monthYear, int finalRegularDays, int finalOvertimeDays, double penalty, double insurance, double tax, double finalSalary, int forgotCheckOutCount, double totalWorkingHours) {
        this.employeeName = employeeName;
        this.monthYear = monthYear;
        this.finalRegularDays = finalRegularDays;
        this.finalOvertimeDays = finalOvertimeDays;
        this.penalty = penalty;
        this.insurance = insurance;
        this.tax = tax;
        this.finalSalary = finalSalary;
        this.forgotCheckOutCount = forgotCheckOutCount;
        this.totalWorkingHours = totalWorkingHours;
    }

    // Getters
    public String getEmployeeName() { return employeeName; }
    public String getMonthYear() { return monthYear; }
    public int getFinalRegularDays() { return finalRegularDays; }
    public int getFinalOvertimeDays() { return finalOvertimeDays; }
    public double getPenalty() { return penalty; }
    public double getInsurance() { return insurance; }
    public double getTax() { return tax; }
    public double getFinalSalary() { return finalSalary; }
    public int getForgotCheckOutCount() { return forgotCheckOutCount; }
    public double getTotalWorkingHours() { return totalWorkingHours; }
}