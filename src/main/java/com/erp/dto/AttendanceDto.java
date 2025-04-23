package com.erp.dto;

import lombok.*;
import java.time.LocalDate;

import com.erp.entity.Attendance;
import com.erp.entity.Attendance.AttendanceStatus;
import com.erp.entity.Employee;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private boolean present;

    private boolean overtime;

    private String remarks;

    public static AttendanceDto fromEntity(Attendance attendance) {
        return AttendanceDto.builder()
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getName())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .present(attendance.getStatus().isCountedAsPresent())
                .overtime(attendance.isOvertime())
                .remarks(attendance.getRemarks())
                .build();
    }

    
    public Attendance toEntity(Employee employee) {
        return Attendance.builder()
                .employee(employee)
                .date(this.date != null ? this.date : LocalDate.now())
                .status(this.status)
                .present(this.status.isCountedAsPresent())
                .overtime(this.overtime)
                .remarks(this.remarks)
                .build();
    }

}
