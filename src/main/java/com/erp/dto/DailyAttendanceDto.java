package com.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyAttendanceDto {
    private LocalDate date;
    private String status;  // Present, Absent, or Not Marked
}
