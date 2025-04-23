package com.erp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payslips")
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private Double baseSalary;
    private Double bonus;
    private Double deduction;
    private Double totalPayable;

    private LocalDate dateIssued;

    private String status; // e.g., "Generated", "Emailed"
    private String downloadUrl;

    @PrePersist
    public void prePersist() {
        if (this.dateIssued == null) {
            this.dateIssued = LocalDate.now();
        }
    }

    public Double calculateNetSalary() {
        return (baseSalary != null ? baseSalary : 0.0) +
               (bonus != null ? bonus : 0.0) -
               (deduction != null ? deduction : 0.0);
    }

    public Double getNetSalary() {
        return calculateNetSalary();
    }

    public int getMonth() {
        return dateIssued != null ? dateIssued.getMonthValue() : 0;
    }

    public int getYear() {
        return dateIssued != null ? dateIssued.getYear() : 0;
    }

    // Getters & setters...
}
