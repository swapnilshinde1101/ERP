package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;

    private Long departmentId;         // Reference to Department
    private String departmentName;     // Useful for UI display

    private Double baseSalary;
    private Double bonus;
    private Double deduction;

    private String performanceRating;

    private Boolean active;

    private String password; // Consider excluding this field in responses
}
