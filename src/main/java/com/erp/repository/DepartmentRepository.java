package com.erp.repository;

import com.erp.entity.Department;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // Custom query methods can go here if necessary
	Optional<Department> findByNameIgnoreCase(String name);


}
