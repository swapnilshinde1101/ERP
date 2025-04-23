package com.erp.repository;

import com.erp.entity.PerformanceReview;
import com.erp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
    List<PerformanceReview> findByEmployeeId(Long employeeId);
}
