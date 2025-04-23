package com.erp.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayslipDto {

    private Long payslipId;

    private Long employeeId;
    private String employeeName;
    private String department;
    private String role;

    private String payslipMonth;     // e.g., "April 2025"
    private LocalDate paymentDate;

    private double baseSalary;
    private double bonus;
    private double deductions;
    private double finalSalary;

    private double performanceRating;
    private String performanceImpact;

    private String status;
    private String downloadUrl;

    private List<PayslipLogDto> logs;  // Optional: For audit trail
}
