package com.erp.repository;

import com.erp.entity.Leave;
import com.erp.entity.Leave.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    boolean existsByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            Long employeeId,
            LocalDate start,
            LocalDate end,
            LeaveStatus status
    );
}
