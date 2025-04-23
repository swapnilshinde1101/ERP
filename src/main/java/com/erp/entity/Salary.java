package com.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.logging.Logger;

@Builder
@Entity
@Table(name = "salary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    private String employeeName;

    @NotNull(message = "Department cannot be null")
    private String department;

    @NotNull(message = "Month cannot be null")
    private LocalDate month;

    @Min(value = 0, message = "Base salary must be a positive value")
    private Double baseSalary;

    private Double bonus = 0.0;  // Default value for bonus
    private Double tax = 0.0;    // Default value for tax
    private Double deduction = 0.0;  // Default value for deduction

    private int presentDays;
    private int leaveDays;
    private int absentDays;
    


    @Column(nullable = false)
    private Double finalSalary; // baseSalary + bonus - deductions

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id", nullable = true)
    private PerformanceReview performanceReview;  // new field for performance review

    private Double totalPayable;
    private boolean approvedByHR;
    private boolean forwardedToFinance;
    private boolean paid;

    private LocalDate date;

    // Logger for business operations
    private static final Logger logger = Logger.getLogger(Salary.class.getName());
    
    
    

 
    public Double getTotalEarnings() {
        Double earnings = baseSalary + (bonus != null ? bonus : 0.0)
                - (tax != null ? tax : 0.0)
                - (deduction != null ? deduction : 0.0);
        
        // Log the earnings calculation
        logger.info("Earnings calculation: " + earnings);
        
        return earnings;
    }

    /**
     * Determines the salary approval status based on internal flags.
     * This is important in real-world use to track the approval workflow.
     */
    public String getStatus() {
        if (!approvedByHR) {
            return "Pending HR Approval";
        }
        if (!forwardedToFinance) {
            return "Awaiting Finance";
        }
        if (paid) {
            return "Paid";
        }
        return "Forwarded to Finance";
    }

    /**
     * Calculates and sets the total payable amount.
     * In real-world systems, this could include other dynamic factors such as bonuses, tax rules, etc.
     */
    public void calculateTotalPayable() {
        this.totalPayable = getTotalEarnings();  // Can be expanded with more business logic
        logger.info("Total payable amount calculated: " + totalPayable);
    }

    /**
     * Validates the salary object. This ensures the object is in a valid state before performing any operations.
     */
    public void validate() {
        if (baseSalary <= 0) {
            throw new IllegalArgumentException("Base salary must be greater than zero");
        }
        if (month.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Month cannot be in the future");
        }
        logger.info("Salary object validated successfully.");
    }

    /**
     * Event triggered when salary is approved by HR.
     * In real-world use, this could trigger notifications or further actions.
     */
    public void approveByHR() {
        if (approvedByHR) {
            throw new IllegalStateException("Salary already approved by HR.");
        }
        approvedByHR = true;
        logger.info("Salary approved by HR for employee: " + employeeName);
    }

    /**
     * Event triggered when salary is forwarded to finance.
     * This would trigger the finance team to process the payment.
     */
    public void forwardToFinance() {
        if (!approvedByHR) {
            throw new IllegalStateException("Salary must be approved by HR first.");
        }
        forwardedToFinance = true;
        logger.info("Salary forwarded to finance for employee: " + employeeName);
    }

    /**
     * Marks salary as paid and triggers a payment action.
     */
    public void markAsPaid() {
        if (!forwardedToFinance) {
            throw new IllegalStateException("Salary must be forwarded to finance before marking as paid.");
        }
        paid = true;
        logger.info("Salary marked as paid for employee: " + employeeName);
    }

	@Override
	public String toString() {
		return "Salary [id=" + id + ", employee=" + employee + ", employeeName=" + employeeName + ", department="
				+ department + ", month=" + month + ", baseSalary=" + baseSalary + ", bonus=" + bonus + ", tax=" + tax
				+ ", deduction=" + deduction + ", presentDays=" + presentDays + ", leaveDays=" + leaveDays
				+ ", absentDays=" + absentDays + ", finalSalary=" + finalSalary + ", performanceReview="
				+ performanceReview + ", totalPayable=" + totalPayable + ", approvedByHR=" + approvedByHR
				+ ", forwardedToFinance=" + forwardedToFinance + ", paid=" + paid + ", date=" + date + "]";
	}
}
