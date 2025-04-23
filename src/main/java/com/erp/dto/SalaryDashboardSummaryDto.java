package com.erp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDashboardSummaryDto {

    private Double totalPayout;
    private Long pendingPayslipsCount;
    private Long forwardedPayslipsCount;
    


    public Double getTotalPayout() {
        return totalPayout;
    }

    public void setTotalPayout(Double totalPayout) {
        this.totalPayout = totalPayout;
    }

    public Long getPendingPayslipsCount() {
        return pendingPayslipsCount;
    }

    public void setPendingPayslipsCount(Long pendingPayslipsCount) {
        this.pendingPayslipsCount = pendingPayslipsCount;
    }

    public Long getForwardedPayslipsCount() {
        return forwardedPayslipsCount;
    }

    public void setForwardedPayslipsCount(Long forwardedPayslipsCount) {
        this.forwardedPayslipsCount = forwardedPayslipsCount;
    }

}
