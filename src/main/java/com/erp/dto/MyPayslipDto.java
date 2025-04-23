package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyPayslipDto {
    private Long id;
    private String month;
    private int year;
    private double netSalary;
    private String status;
    private String downloadUrl;
}
