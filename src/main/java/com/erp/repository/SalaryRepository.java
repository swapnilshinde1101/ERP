package com.erp.repository;

import com.erp.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    // Find salaries by month and year
//	@Query("SELECT s FROM Salary s WHERE MONTH(s.salaryDate) = :month AND YEAR(s.salaryDate) = :year")
//	List<Salary> findByMonthAndYear(@Param("month") int month, @Param("year") int year);


    // Find salaries by date range
    List<Salary> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Find salaries by employee ID
    List<Salary> findByEmployeeId(Long employeeId);

    // Check if salary exists for a specific employee and month
    boolean existsByEmployeeIdAndMonth(Long employeeId, LocalDate month);

    // Find salaries by month, HR approved status, and employee name
    List<Salary> findByMonthAndApprovedByHRAndEmployeeName(LocalDate month, Boolean approvedByHR, String employeeName);

    // Sum of total payout across all salaries
    @Query("SELECT SUM(s.totalPayable) FROM Salary s WHERE s.paid = true")
    Double sumTotalPayout();

    // Count of pending payslips (approvedByHR = false)
    @Query("SELECT COUNT(s) FROM Salary s WHERE s.approvedByHR = false")
    Long countPendingPayslips();

    // Count of paid payslips (approvedByHR = true)
    @Query("SELECT COUNT(s) FROM Salary s WHERE s.approvedByHR = true")
    Long countPaidPayslips();

    // Count based on a specific approval status (approvedByHR)
    Long countByApprovedByHR(Boolean status);

    // Find salaries by month and payment status (approvedByHR)
    List<Salary> findByMonthAndApprovedByHR(LocalDate month, Boolean approvedByHR);

    // Count salaries by paid status (paid or pending)
    Long countByPaid(boolean status); // countByPaid(true) = paid, false = pending

    // Find salaries by employee ID, ordered by date (descending)
    List<Salary> findByEmployeeIdOrderByDateDesc(Long employeeId);

//	List<Salary> findByMonth(int monthValue, int year);
}
