package com.erp.service;

import com.erp.dto.PerformanceReviewDto;
import com.erp.entity.Employee;
import com.erp.entity.PerformanceReview;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceReviewService {

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

 // Convert PerformanceReview entity to DTO
    private PerformanceReviewDto convertToDto(PerformanceReview review) {
        Employee employee = review.getEmployee();
        return PerformanceReviewDto.builder()
                .id(review.getId())
                .employeeId(employee.getId())
                .reviewDate(review.getReviewDate())
                .performanceRating(review.getPerformanceRating())
                .comments(review.getComments())
                .bonusAmount(employee.getBonus())               // Add bonus amount
                .performanceImpact(employee.getPerformanceImpact()) // Add performance impact
                .finalSalary(employee.getFinalSalary())         // Add final salary
                .build();
    }

    @Transactional
    public PerformanceReview createPerformanceReview(PerformanceReviewDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + dto.getEmployeeId()));

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .reviewDate(dto.getReviewDate())
                .performanceRating(dto.getPerformanceRating())
                .comments(dto.getComments())
                .build();

        // Save review
        PerformanceReview saved = performanceReviewRepository.save(review);

        // 📌 Update employee's salary rating logic (example: performanceBonus adjustment)
        updateEmployeePerformanceImpact(employee, dto.getPerformanceRating());

        // Update final salary (performance-based)
        updateFinalSalary(employee);

        return saved;
    }

    private void updateEmployeePerformanceImpact(Employee employee, String rating) {
        double bonus = 0.0;
        double numericRating = 0.0;

        switch (rating.toLowerCase()) {
            case "excellent" -> {
                bonus = 1000.0;
                numericRating = 5.0;
            }
            case "good" -> {
                bonus = 500.0;
                numericRating = 4.0;
            }
            case "average" -> {
                bonus = 0.0;
                numericRating = 3.0;
            }
            case "poor" -> {
                bonus = -500.0;
                numericRating = 2.0;
            }
            default -> numericRating = 0.0;
        }

        employee.setBonus(bonus);
        employee.setPerformanceRating(numericRating);
        employeeRepository.save(employee); // Persist updated salary/bonus
    }

    private void updateFinalSalary(Employee employee) {
        // Assuming final salary is baseSalary + bonus - deductions
        double finalSalary = employee.getBaseSalary() + employee.getBonus() - employee.getDeduction();
        employee.setFinalSalary(finalSalary);
        employeeRepository.save(employee); // Persist updated final salary
    }

    @Transactional(readOnly = true)
    public List<PerformanceReviewDto> getAllReviews() {
        return performanceReviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PerformanceReviewDto getReviewById(Long id) {
        PerformanceReview review = performanceReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));
        return convertToDto(review);
    }

    @Transactional(readOnly = true)
    public List<PerformanceReviewDto> getReviewsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        return performanceReviewRepository.findByEmployee(employee).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PerformanceReviewDto> getReviewsByEmployeeEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
        return performanceReviewRepository.findByEmployee(employee).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
