package com.erp.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentAttendanceDto {
	
    private Map<String, Map<String, Long>> departmentAttendance;

    private Map<String, Map<String, Long>> departmentStatus;
    
 

}
