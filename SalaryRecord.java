package BaiTapLon;

// A simple data class (POJO) to hold the results of a salary calculation.
public class SalaryRecord {
    private final String employeeName;
    private final String monthYear;
    private final int finalRegularDays;
    private final int finalOvertimeDays;
    private final double penalty;
    private final double finalSalary;

    public SalaryRecord(String employeeName, String monthYear, int finalRegularDays, int finalOvertimeDays, double penalty, double finalSalary) {
        this.employeeName = employeeName;
        this.monthYear = monthYear;
        this.finalRegularDays = finalRegularDays;
        this.finalOvertimeDays = finalOvertimeDays;
        this.penalty = penalty;
        this.finalSalary = finalSalary;
    }

    // Getters
    public String getEmployeeName() { return employeeName; }
    public String getMonthYear() { return monthYear; }
    public int getFinalRegularDays() { return finalRegularDays; }
    public int getFinalOvertimeDays() { return finalOvertimeDays; }
    public double getPenalty() { return penalty; }
    public double getFinalSalary() { return finalSalary; }
}