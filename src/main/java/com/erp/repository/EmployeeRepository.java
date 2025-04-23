package com.erp.repository;

import com.erp.entity.Employee;
import com.erp.entity.USER_ROLE;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find employees by role and department name (department is a String)
    Page<Employee> findByRoleAndDepartment_Name(USER_ROLE role, String departmentName, Pageable pageable);

    // Find employee by email, using Optional to handle null cases properly
    Optional<Employee> findByEmail(String email);

    // Find all active employees
    List<Employee> findAllByIsActiveTrue();

    // Optional method to find employee by ID
    Optional<Employee> findById(Long id);
}
