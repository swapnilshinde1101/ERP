package com.erp.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAttendanceDto {
    private Map<String, Map<String, Long>> dailyStatus;
}
