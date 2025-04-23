package com.erp.repository;

import com.erp.entity.Attendance;

import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeId(Long employeeId);
    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Attendance> findAll(Pageable pageable);
    List<Attendance> findByEmployeeIdAndPresent(Long employeeId, boolean present);
    List<Attendance> findByEmployeeIdAndStatus(Long employeeId, Attendance.AttendanceStatus status);
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    

    
Page<Attendance> findByDateBetweenAndEmployeeIdAndStatus(LocalDate startDate, LocalDate endDate, Long employeeId, Attendance.AttendanceStatus status, Pageable pageable);
    
    Page<Attendance> findByEmployeeIdAndStatus(Long employeeId, Attendance.AttendanceStatus status, Pageable pageable);
    
    Page<Attendance> findByStatus(Attendance.AttendanceStatus status, Pageable pageable);
    
    List<Attendance> findByDate(LocalDate date);
    
 // Method to check presence
    @Query("SELECT COUNT(a) > 0 FROM Attendance a " +
    	       "WHERE a.employee.id = :employeeId AND a.date = :date " +
    	       "AND a.status = 'PRESENT'")
    	boolean isEmployeePresentOnDate(@Param("employeeId") Long employeeId,
    	                                @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE FUNCTION('MONTH', a.date) = :month AND FUNCTION('YEAR', a.date) = :year AND a.employee.id = :employeeId AND a.status = 'PRESENT'")
    int countPresentDaysByMonthAndYearAndEmployeeId(@Param("month") int month, @Param("year") int year, @Param("employeeId") Long employeeId);





}
