package com.erp.repository;

import com.erp.entity.Employee;
import com.erp.entity.EmployeeLeave;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {
    // Method to find EmployeeLeave records based on Employee and a date range
    List<EmployeeLeave> findByEmployeeAndStartDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
    List<EmployeeLeave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    Page<EmployeeLeave> findByEmployee(Employee employee, Pageable pageable);

}

