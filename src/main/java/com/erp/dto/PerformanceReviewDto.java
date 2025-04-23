package com.erp.dto;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public class PerformanceReviewDto {

    private Long id;
    private Long employeeId;
    private LocalDate reviewDate;
    private String performanceRating;
    private String comments;
    
    // Newly added fields
    private Double bonusAmount;
    private Double performanceImpact;
    private Double finalSalary;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(String performanceRating) {
        this.performanceRating = performanceRating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Double getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(Double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public Double getPerformanceImpact() {
        return performanceImpact;
    }

    public void setPerformanceImpact(Double performanceImpact) {
        this.performanceImpact = performanceImpact;
    }

    public Double getFinalSalary() {
        return finalSalary;
    }

    public void setFinalSalary(Double finalSalary) {
        this.finalSalary = finalSalary;
    }
}
