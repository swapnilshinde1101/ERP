package com.erp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.erp.dto.EmployeeLeaveDto;
import com.erp.service.EmployeeLeaveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.erp.dto.EmployeeLeaveDto;
import com.erp.service.EmployeeLeaveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/leaves")
public class EmployeeLeaveController {

    @Autowired
    private final EmployeeLeaveService employeeLeaveService;

    // ✅ Employee requests leave
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<EmployeeLeaveDto> requestLeave(@Valid @RequestBody EmployeeLeaveDto dto) {
        return ResponseEntity.ok(employeeLeaveService.createLeaveRequest(dto));
    }

    // ✅ HR or Admin approves or rejects leave
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<EmployeeLeaveDto> updateLeaveStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(employeeLeaveService.updateLeaveStatus(id, status));
    }

    // ✅ Admin or HR can view all leave requests
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @GetMapping
    public ResponseEntity<Page<EmployeeLeaveDto>> getAllLeaveRequests(Pageable pageable) {
        return ResponseEntity.ok(employeeLeaveService.getAllLeaves(pageable));
    }

    // ✅ (Optional) Employee can view their own leave requests
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my")
    public ResponseEntity<Page<EmployeeLeaveDto>> getMyLeaves(Pageable pageable) {
        return ResponseEntity.ok(employeeLeaveService.getMyLeaves(pageable));
    }
    
 // ✅ HR or Admin views leave requests for a specific date range
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<List<EmployeeLeaveDto>> getLeaveRequestsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<EmployeeLeaveDto> leaveRequests = employeeLeaveService.getLeaveRequestsByDateRange(startDate, endDate);
        return ResponseEntity.ok(leaveRequests);
    }

    
}
