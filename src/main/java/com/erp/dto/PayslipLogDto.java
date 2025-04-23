package com.erp.dto;

import java.time.LocalDateTime;

import com.erp.entity.PayslipLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayslipLogDto {
	
	public PayslipLogDto(PayslipLog.Action action, String doneBy, LocalDateTime timestamp) {
	    this.action = action.name(); // Convert enum to String
	    this.doneBy = doneBy;
	    this.timestamp = timestamp;
	}


    private String action;         // e.g. "GENERATED", "EMAILED", "DOWNLOADED"
    private String doneBy;         // e.g. "hr@gmail.com"
    private LocalDateTime timestamp;
}
