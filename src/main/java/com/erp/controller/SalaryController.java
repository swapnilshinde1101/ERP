package com.erp.controller;

import com.erp.dto.*;
import com.erp.entity.Salary;
import com.erp.service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.io.IOException;


@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;
    private final PayslipService payslipService;

    // Generate salary for a month
    @PostMapping("/generate")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> generateSalary(
            @RequestParam("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        try {
            salaryService.generateMonthlySalary(month);
            return ResponseEntity.ok("Salary generated successfully for " + month.getMonth());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate salary for " + month.getMonth() + ": " + e.getMessage());
        }
    }


//    // Get all salaries by month
//    @GetMapping("/all")
//    @PreAuthorize("hasAnyRole('HR', 'FINANCE','ADMIN')")
//    public ResponseEntity<List<Salary>> getAllSalaries(
//            @RequestParam("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
//        return ResponseEntity.ok(salaryService.getAllSalaries(month));
//    }


    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> approveSalary(@PathVariable Long id) {
        salaryService.approveSalary(id);
        return ResponseEntity.ok("Salary approved successfully");
    }

    @PutMapping("/{id}/forward")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> forwardSalary(@PathVariable Long id) {
        salaryService.forwardSalaryToFinance(id);
        return ResponseEntity.ok("Salary forwarded to Finance successfully");
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<String> markSalaryAsPaid(@PathVariable Long id) {
        salaryService.markAsPaid(id);
        return ResponseEntity.ok("Salary marked as paid");
    }

  

  
    // Summary dashboard
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('HR', 'FINANCE')")
    public ResponseEntity<Map<String, Long>> getSummary(
            @RequestParam("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        return ResponseEntity.ok(salaryService.getSalarySummary(month));
    }

    
    // Dashboard for admin/finance
    @GetMapping("/salary/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'HR')")
    public ResponseEntity<SalaryDashboardSummaryDto> getSalaryDashboardSummary() {
        return ResponseEntity.ok(salaryService.getSalaryDashboardSummary());
    }

       @GetMapping("/employee/{employeeId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Salary>> getSalaryHistoryByEmployee(@PathVariable Long employeeId) {
        List<Salary> salaryHistory = salaryService.getSalaryHistoryByEmployeeId(employeeId);
        return ResponseEntity.ok(salaryHistory);
    }

    @PutMapping("/regenerate/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> regenerateSalary(@PathVariable Long id) {
        salaryService.regenerateSalaryEntry(id); // Call to service layer to regenerate salary
        return ResponseEntity.ok("Salary entry regenerated successfully");
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSalaryEntry(@PathVariable Long id) {
        salaryService.deleteSalary(id); // Call to the service to delete the salary
        return ResponseEntity.ok("Salary entry deleted successfully");
    }


   


    
    
    @GetMapping("/status-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'FINANCE')")
    public ResponseEntity<?> getSalaryStatusCount(
            @RequestParam("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        try {
            Map<String, Long> salaryStatusCounts = salaryService.getSalaryStatusCounts(month);
            return ResponseEntity.ok(salaryStatusCounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Failed to fetch salary status counts"));
        }
    }




}
