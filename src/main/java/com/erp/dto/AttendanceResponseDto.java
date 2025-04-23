package com.erp.dto;

import lombok.*;
import java.time.LocalDate;

import com.erp.entity.Attendance;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class AttendanceResponseDto {
	
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private String status;
    private boolean present;
    
    
	public AttendanceResponseDto(Long employeeId, String employeeName, LocalDate date, String status, boolean present) {
		super();
		this.employeeId = employeeId;
		this.employeeName = employeeName;
		this.date = date;
		this.status = status;
		this.present = present;
	}    
    
	 public static AttendanceResponseDto fromEntity(Attendance attendance) {
	        return AttendanceResponseDto.builder()
//	                .id(attendance.getId())
	                .employeeId(attendance.getEmployee().getId())
	                .employeeName(attendance.getEmployee().getName())
	                .date(attendance.getDate())
	                .status(attendance.getStatus().toString())
	                .present(attendance.isPresent())
	                .build();
	    }
    
    
}
