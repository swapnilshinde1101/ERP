package com.erp.repository;

import com.erp.entity.Payslip;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    List<Payslip> findByEmployeeId(Long employeeId);

}
