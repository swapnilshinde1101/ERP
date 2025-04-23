package com.erp.dto;

public class SalaryDto {

    private Long employeeId;
    private Double baseSalary;
    private Double bonusAmount;
    private Double deduction;
    private Double finalSalary;

    // Newly added fields
    private Double performanceImpact;

    // Getters and Setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(Double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public Double getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(Double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public Double getDeduction() {
        return deduction;
    }

    public void setDeduction(Double deduction) {
        this.deduction = deduction;
    }

    public Double getFinalSalary() {
        return finalSalary;
    }

    public void setFinalSalary(Double finalSalary) {
        this.finalSalary = finalSalary;
    }

    public Double getPerformanceImpact() {
        return performanceImpact;
    }

    public void setPerformanceImpact(Double performanceImpact) {
        this.performanceImpact = performanceImpact;
    }

    // Optional Builder pattern
    public static class Builder {
        private Long employeeId;
        private Double baseSalary;
        private Double bonusAmount;
        private Double deduction;
        private Double finalSalary;
        private Double performanceImpact;

        public Builder employeeId(Long employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder baseSalary(Double baseSalary) {
            this.baseSalary = baseSalary;
            return this;
        }

        public Builder bonusAmount(Double bonusAmount) {
            this.bonusAmount = bonusAmount;
            return this;
        }

        public Builder deduction(Double deduction) {
            this.deduction = deduction;
            return this;
        }

        public Builder finalSalary(Double finalSalary) {
            this.finalSalary = finalSalary;
            return this;
        }

        public Builder performanceImpact(Double performanceImpact) {
            this.performanceImpact = performanceImpact;
            return this;
        }

        public SalaryDto build() {
            SalaryDto dto = new SalaryDto();
            dto.employeeId = this.employeeId;
            dto.baseSalary = this.baseSalary;
            dto.bonusAmount = this.bonusAmount;
            dto.deduction = this.deduction;
            dto.finalSalary = this.finalSalary;
            dto.performanceImpact = this.performanceImpact;
            return dto;
        }
    }
}
