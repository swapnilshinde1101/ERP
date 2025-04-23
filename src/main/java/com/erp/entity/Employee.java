package com.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private USER_ROLE role;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @JsonBackReference
    private Department department;

    @OneToMany(mappedBy = "employee")
    private List<Salary> salaries;

    private Boolean isActive;

    @Column(nullable = false)
    private String password;

    private Double baseSalary;
    private Double bonus;
    private Double deduction;
    
    private Double performanceRating;
    private Double finalSalary;
    
    private Double performanceImpact; 


    // Change from performanceRating to a list of performance reviews
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<PerformanceReview> performanceReviews; // Store multiple performance reviews

    // Add method to get the latest performance review
    public PerformanceReview getLatestPerformanceReview() {
        if (performanceReviews != null && !performanceReviews.isEmpty()) {
            return performanceReviews.stream()
                    .max((r1, r2) -> r1.getReviewDate().compareTo(r2.getReviewDate()))
                    .orElse(null); // return null if no reviews are present
        }
        return null;
    }

    // Add method to get employee's name (for generating payslip filenames)
    public String getEmployeeName() {
        return this.name;  // Assuming 'name' is the field storing the employee's name
    }
    public void setPerformanceRating(Double performanceRating) {
        this.performanceRating = performanceRating;
    }
    public void setFinalSalary(Double finalSalary) {
        this.finalSalary = finalSalary;
    }

    

    // Getter and Setter for performanceImpact
    public Double getPerformanceImpact() {
        return performanceImpact; // Return calculated value or a fixed one
    }

    public void setPerformanceImpact(Double performanceImpact) {
        this.performanceImpact = performanceImpact;
    }

	@Override
	public String toString() {
		return "Employee [id=" + id + ", name=" + name + ", email=" + email + ", phone=" + phone + ", role=" + role
				+ ", department=" + department + ", salaries=" + salaries + ", isActive=" + isActive + ", password="
				+ password + ", baseSalary=" + baseSalary + ", bonus=" + bonus + ", deduction=" + deduction
				+ ", performanceRating=" + performanceRating + ", finalSalary=" + finalSalary + ", performanceImpact="
				+ performanceImpact + ", performanceReviews=" + performanceReviews + "]";
	}

}
