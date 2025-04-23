package com.erp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.erp.dto.MyPayslipDto;
import com.erp.dto.PayslipLogDto;
import com.erp.dto.SalaryDashboardSummaryDto;
import com.erp.dto.SalaryPayslipDto;
import com.erp.entity.Attendance;
import com.erp.entity.Employee;
import com.erp.entity.Payslip;
import com.erp.entity.PayslipLog;
import com.erp.entity.Salary;
import com.erp.entity.SalaryCalculator;
import com.erp.exception.EmployeeNotFoundException;
import com.erp.exception.SalaryNotFoundException;
import com.erp.repository.AttendanceRepository;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.PayslipLogRepository; // Added
import com.erp.repository.PayslipRepository; // Added
import com.erp.repository.SalaryRepository;
import com.erp.service.SalaryService;
import com.itextpdf.text.log.SysoCounter;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class SalaryService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryService.class);
    
    @Autowired
    private  SalaryCalculator salaryCalculator; 

    @Autowired
    private  EmployeeRepository employeeRepository;
    
    @Autowired
    private  AttendanceRepository attendanceRepository;
    
    @Autowired
    private  SalaryRepository salaryRepository;
    
    @Autowired
    private  PayslipRepository payslipRepository; // Added
    
    @Autowired
    private  PayslipLogRepository payslipLogRepository; // Added

    
   
    public void generateMonthlySalary(LocalDate monthDate) {

        try {
            YearMonth ym = YearMonth.from(monthDate);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<Employee> employees = employeeRepository.findAllByIsActiveTrue();
            for (Employee emp : employees) {
                if (salaryRepository.existsByEmployeeIdAndMonth(emp.getId(), start)) {
                    continue;  // Skip if salary already generated
                }
                List<Attendance> attendanceList = attendanceRepository.findByEmployeeIdAndDateBetween(emp.getId(), start, end);

                if (attendanceList.isEmpty()) {

                    logger.warn("No attendance found for Employee ID: {} for the month of {}", emp.getId(), start);

                    continue;

                }
                // Calculate attendance stats
                long presentDays = attendanceList.stream()
                        .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT)
                        .count();
                long leaveDays = attendanceList.stream()
                        .filter(a -> a.getStatus() == Attendance.AttendanceStatus.LEAVE)
                        .count();
                long absentDays = attendanceList.stream()
                        .filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT)
                        .count();

                // Calculate salary

                double grossSalary = salaryCalculator.calculateGrossSalary(emp, attendanceList, ym);

                double tax = salaryCalculator.calculateMonthlyTax(emp);

                double netSalary = salaryCalculator.calculateNetSalary(emp, attendanceList, ym);



                Salary salary = Salary.builder()

                        .employeeName(emp.getName())

                        .department(emp.getDepartment().getName())

                        .month(start)

                        .baseSalary(emp.getBaseSalary() != null ? emp.getBaseSalary() : 0.0)

                        .presentDays((int) presentDays)

                        .leaveDays((int) leaveDays)

                        .absentDays((int) absentDays)

                        .totalPayable(netSalary)

                        .approvedByHR(false)

                        .forwardedToFinance(false)

                        .paid(false)

                        .date(LocalDate.now())

                        .bonus(emp.getBonus() != null ? emp.getBonus() : 0.0)

                        .tax(tax)

                        .deduction(emp.getDeduction() != null ? emp.getDeduction() : 0.0)

                        .build();

                salaryRepository.save(salary);



                logger.info("Generated salary for Employee ID: {} for month {}. Net Payable: {}", 

                    emp.getId(), ym, netSalary);

            }

        } catch (Exception e) {

            logger.error("Error generating salary for month {}: {}", monthDate, e.getMessage(), e);

        }

    }


   

//    public List<Salary> getAllSalaries(LocalDate month) {
//        int year = month.getYear();
//        int monthValue = month.getMonthValue();
//        return salaryRepository.findByMonth(monthValue, year);
//    }

    public void approveSalary(Long id) {
        Optional<Salary> optionalSalary = salaryRepository.findById(id);
        if (optionalSalary.isPresent()) {
            Salary salary = optionalSalary.get();
            salary.setApprovedByHR(true);
            salaryRepository.save(salary);
        } else {
            System.out.println("Salary not found for ID: " + id);
            throw new SalaryNotFoundException("Salary not found");
        }
    }


    public void forwardSalaryToFinance(Long id) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new SalaryNotFoundException("Salary not found"));
        if (!salary.isApprovedByHR()) {
            throw new RuntimeException("Salary must be approved by HR first.");
        }
        salary.setForwardedToFinance(true);
        salaryRepository.save(salary);
    }

    
   


    public void markAsPaid(Long id) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new SalaryNotFoundException("Salary not found"));
        if (!salary.isForwardedToFinance()) {
            throw new RuntimeException("Salary not forwarded to Finance yet.");
        }
        salary.setPaid(true);
        salaryRepository.save(salary);
    }



    public Map<String, Long> getSalarySummary(LocalDate month) {
        // Calculate the start and end date of the month
        LocalDate startDate = month.withDayOfMonth(1); // First day of the month
        LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth()); // Last day of the month

        // Fetch the salaries for the given month using a date range
        List<Salary> salaries = salaryRepository.findByDateBetween(startDate, endDate);

        // Count the total, approved, forwarded, and paid salaries
        long total = salaries.size();
        long approved = salaries.stream().filter(Salary::isApprovedByHR).count();
        long forwarded = salaries.stream().filter(Salary::isForwardedToFinance).count();
        long paid = salaries.stream().filter(Salary::isPaid).count();

        // Create the summary map
        Map<String, Long> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("approved", approved);
        summary.put("forwarded", forwarded);
        summary.put("paid", paid);

        return summary;
    }


    
    public List<SalaryPayslipDto> getSalaryHistoryForEmployee(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID must not be null");
        }

        List<Salary> salaries = salaryRepository.findByEmployeeId(employeeId);

        return salaries.stream()
                .sorted(Comparator.comparing(Salary::getMonth).reversed())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private SalaryPayslipDto mapToDto(Salary salary) {
        return SalaryPayslipDto.builder()
                .id(salary.getId())
                .employeeName(salary.getEmployeeName())
                .employeeEmail(salary.getEmployee().getEmail()) // ✅ assuming Employee has getEmail()
                .department(salary.getDepartment())
                .month(salary.getMonth().format(DateTimeFormatter.ofPattern("MMMM yyyy")))

                .baseSalary(salary.getBaseSalary() != null ? salary.getBaseSalary() : 0.0)
                .bonus(salary.getBonus() != null ? salary.getBonus() : 0.0)
                .tax(salary.getTax() != null ? salary.getTax() : 0.0)
                .deduction(salary.getDeduction() != null ? salary.getDeduction() : 0.0)

                .presentDays(salary.getPresentDays())
                .absentDays(salary.getAbsentDays())
                .leaveDays(salary.getLeaveDays())

                .totalEarnings(salary.getTotalEarnings())
                .netSalary(salary.getTotalEarnings()) // or subtract tax/deductions again if needed

                .approvedByHR(salary.isApprovedByHR())
                .forwardedToFinance(salary.isForwardedToFinance())
                .paid(salary.isPaid())

                .status(salary.getStatus())
                .downloadUrl("/api/salary/download/" + salary.getId())
                .build();
    }

    
   
    
 


    
    public SalaryDashboardSummaryDto getSalaryDashboardSummary() {
        Double totalPayout = salaryRepository.sumTotalPayout();
        Long pendingPayslipsCount = salaryRepository.countByPaid(false); // Pending
        Long paidPayslipsCount = salaryRepository.countByPaid(true); // Paid

        return new SalaryDashboardSummaryDto(totalPayout, pendingPayslipsCount, paidPayslipsCount);
    }

    
    
    
    

    

    public List<MyPayslipDto> getMyPayslips(String email) {
        // Fetch employee using their email address
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Fetch payslips for the employee using their employee ID
        List<Payslip> payslips = payslipRepository.findByEmployeeId(employee.getId());

        // Map payslips to MyPayslipDto
        return payslips.stream()
        		.map(p -> new MyPayslipDto(
        			    p.getId(),
        			    Month.of(p.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH), // "April"
        			    p.getYear(),
        			    p.getNetSalary(),
        			    p.getStatus(),
        			    "/api/payslip/download/" + p.getId()
        			))

                .collect(Collectors.toList());
    }


    
    public void logAction(Long payslipId, String action, String doneBy) {
        // Fetch the payslip based on the payslipId (assuming you have a Payslip entity)
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payslip ID"));

        // Convert the string action to the corresponding Action enum
        PayslipLog.Action actionEnum = PayslipLog.Action.valueOf(action);

        // Create a new PayslipLog
        PayslipLog log = new PayslipLog();
        log.setPayslip(payslip);
        log.setAction(actionEnum);  // Set the action as Enum
        log.setDoneBy(doneBy);
        log.setTimestamp(LocalDateTime.now());

        // Save the log
        payslipLogRepository.save(log);
    }



    public List<PayslipLogDto> getLogsByPayslipId(Long payslipId) {
        List<PayslipLog> logs = payslipLogRepository.findByPayslipIdOrderByTimestampDesc(payslipId);

        return logs.stream()
                .map(log -> new PayslipLogDto(
                        log.getAction(),
                        log.getDoneBy(),
                        log.getTimestamp()
                )).collect(Collectors.toList());
    }

    public List<PayslipLogDto> getLogsByEmployee(Long employeeId) {
        List<Payslip> payslips = payslipRepository.findByEmployeeId(employeeId);
        List<Long> ids = payslips.stream().map(Payslip::getId).collect(Collectors.toList());

        List<PayslipLog> logs = payslipLogRepository.findByPayslipIdInOrderByTimestampDesc(ids);

        return logs.stream()
                .map(log -> new PayslipLogDto(
                        log.getAction(),
                        log.getDoneBy(),
                        log.getTimestamp()
                )).collect(Collectors.toList());
    }
    

    public List<PayslipLogDto> getFilteredLogs(Long payslipId, Integer month, Integer year, String actionType) {
        List<PayslipLog> logs = payslipLogRepository.findByPayslipId(payslipId);

        return logs.stream()
                .filter(log -> (month == null || log.getTimestamp().getMonthValue() == month)
                        && (year == null || log.getTimestamp().getYear() == year)
                        && (actionType == null || actionType.isBlank()
                            || log.getAction().name().equalsIgnoreCase(actionType)))
                .map(log -> new PayslipLogDto(
                        log.getAction().name(),  // convert enum to String
                        log.getDoneBy(),
                        log.getTimestamp()))
                .collect(Collectors.toList());
    }


    
    
    public List<PayslipLogDto> getFilteredLogsByEmployee(Long employeeId, Integer month, Integer year, String actionType) {
        List<PayslipLog> logs = payslipLogRepository.findByEmployeeId(employeeId);

        return logs.stream()
                .filter(log -> (month == null || log.getTimestamp().getMonthValue() == month)
                        && (year == null || log.getTimestamp().getYear() == year)
                        && (actionType == null || actionType.isBlank() 
                            || log.getAction().name().equalsIgnoreCase(actionType)))
                .map(log -> new PayslipLogDto(
                        log.getAction().name(),  // convert enum to string
                        log.getDoneBy(),
                        log.getTimestamp()))
                .collect(Collectors.toList());
    }


    public String getEmployeeEmailBySalaryId(Long id) {
        // Assuming you have a repository to fetch the salary or employee data
        Salary salary = salaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Salary not found"));
        return salary.getEmployee().getEmail(); // Assuming Salary has an Employee object with email
    }

	
    public List<Salary> getSalaryHistoryByEmployeeId(Long employeeId) {
        // Fetch the salary history for the employee by their ID
        return salaryRepository.findByEmployeeIdOrderByDateDesc(employeeId);
    }
    
    // Regenerate salary entry for the given ID
    public void regenerateSalaryEntry(Long id) {
        // Fetch the existing salary entry based on the ID
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary entry not found for ID: " + id));

        // Regenerate the salary based on business logic (this is an example, adjust as needed)
        // For instance, resetting the "paid" status, recalculating the total pay, etc.
        salary.setPaid(false); // Example: resetting the "paid" status
        salary.setApprovedByHR(false); // Example: resetting the HR approval status

        // You can also recalculate other fields like bonus, deductions, etc., based on business logic
        salary.calculateTotalPayable();  // If you want to update the total payable based on any changes

        // Save the updated salary entry back to the database
        salaryRepository.save(salary);
    }
    
    // Delete a salary entry by its ID
    public void deleteSalary(Long id) {
        // Fetch the salary entry from the repository
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary entry not found for ID: " + id));

        // Delete the salary entry
        salaryRepository.delete(salary);
    }
    
    public Map<String, Long> getSalaryStatusCounts(LocalDate month) {
        // Calculate the start and end date of the month
        LocalDate startDate = month.withDayOfMonth(1); // First day of the month
        LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth()); // Last day of the month

        // Fetch the salaries for the given month
        List<Salary> salaries = salaryRepository.findByDateBetween(startDate, endDate);

        // Group the salaries by status and count them
        return salaries.stream()
                       .collect(Collectors.groupingBy(Salary::getStatus, Collectors.counting()));
    }



    
}
