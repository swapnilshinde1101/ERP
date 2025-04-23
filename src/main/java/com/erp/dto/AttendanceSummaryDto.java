package com.erp.dto;

import lombok.*;

import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceSummaryDto {
	   private long present;
	    private long absent;
	    private long leave;
	    private long notMarked;
	    
	    }
