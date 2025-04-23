package com.erp.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaryPayslipDto {

    private Long id;
    private String employeeName;
    private String employeeEmail;  // ✅ Add this line
    private String department;
    private String month; // e.g., "April 2025"

    private double baseSalary;
    private double bonus;
    private double tax;
    private double deduction;

    private int presentDays;
    private int absentDays;
    private int leaveDays;

    private double totalEarnings;
    private boolean approvedByHR;
    private boolean forwardedToFinance;
    private boolean paid;

    private double netSalary;
    private String status;
    private String downloadUrl;

   
}
