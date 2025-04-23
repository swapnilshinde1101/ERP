package com.erp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erp.entity.PayslipLog;

public interface PayslipLogRepository extends JpaRepository<PayslipLog, Long> {
	
	List<PayslipLog> findByPayslipIdOrderByTimestampDesc(Long payslipId);
	List<PayslipLog> findByPayslipIdInOrderByTimestampDesc(List<Long> payslipIds);
	List<PayslipLog> findByPayslipId(Long payslipId);
	List<PayslipLog> findByEmployeeId(Long employeeId);
    List<PayslipLog> findByEmployee_Id(Long employeeId); // ✅ Correct method





}
