package com.erp.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class SalaryCalculator {

    @Value("${performance.bonus.rate}")
    private double performanceBonusRate;

    @Value("${performance.rating.threshold}")
    private double performanceBonusThreshold;

    private static final double OVERTIME_BONUS_PER_DAY = 300.0;
    private static final double LEAVE_DEDUCTION_PER_DAY = 500.0;
    private static final double HALF_DAY_DEDUCTION_FACTOR = 0.5;

    public double calculateTax(double annualSalary) {
        if (annualSalary <= 250000) return 0;
        if (annualSalary <= 500000) return (annualSalary - 250000) * 0.05;
        if (annualSalary <= 1000000) return (250000 * 0.05) + (annualSalary - 500000) * 0.2;
        return (250000 * 0.05) + (500000 * 0.2) + (annualSalary - 1000000) * 0.3;
    }

    public double calculateMonthlyTax(Employee employee) {
        double baseSalary = employee.getBaseSalary() != null ? employee.getBaseSalary() : 0;
        double grossAnnual = baseSalary * 12;
        return calculateTax(grossAnnual) / 12;
    }

    public double calculateOvertimeBonus(List<Attendance> attendanceList) {
        return attendanceList.stream()
                .filter(Attendance::isOvertime)
                .count() * OVERTIME_BONUS_PER_DAY;
    }

    public double calculateLeaveDeduction(List<Attendance> attendanceList) {
        long fullLeaves = attendanceList.stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT)
                .count();

        long halfLeaves = attendanceList.stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.HALF_DAY)
                .count();

        return (fullLeaves * LEAVE_DEDUCTION_PER_DAY) +
               (halfLeaves * LEAVE_DEDUCTION_PER_DAY * HALF_DAY_DEDUCTION_FACTOR);
    }
    
    
    public double calculatePerformanceBonus(Employee employee) {
        PerformanceReview latestReview = employee.getLatestPerformanceReview();
        if (latestReview == null) return 0.0;

        String rating = latestReview.getPerformanceRating();
        double base = employee.getBaseSalary() != null ? employee.getBaseSalary() : 0;

        switch (rating.toLowerCase()) {
            case "excellent":
                return base * performanceBonusRate;
            case "good":
                return base * (performanceBonusRate / 2);
            case "average":
                return 0.0; // No bonus for average performance
            default:
                return 0.0; // No bonus for unrecognized ratings
        }
    }


    public double calculateGrossSalary(Employee employee, List<Attendance> attendanceList, YearMonth month) {
        double base = employee.getBaseSalary() != null ? employee.getBaseSalary() : 0;
        double bonus = employee.getBonus() != null ? employee.getBonus() : 0;
        double deduction = employee.getDeduction() != null ? employee.getDeduction() : 0;

        double overtimeBonus = calculateOvertimeBonus(attendanceList);
        double leaveDeduction = calculateLeaveDeduction(attendanceList);
        double performanceBonus = calculatePerformanceBonus(employee);

        return base + bonus + overtimeBonus + performanceBonus - deduction - leaveDeduction;
    }

    public double calculateNetSalary(Employee employee, List<Attendance> attendanceList, YearMonth month) {
        double gross = calculateGrossSalary(employee, attendanceList, month);
        double tax = calculateMonthlyTax(employee);
        return gross - tax;
    }

    public int getTotalWorkingDays(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        int workingDays = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        return workingDays;
    }
}
