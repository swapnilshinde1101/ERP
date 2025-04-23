package com.erp.service;

import com.erp.dto.EmployeeLeaveDto;
import com.erp.entity.Employee;
import com.erp.entity.EmployeeLeave;
import com.erp.exception.EmployeeNotFoundException;
import com.erp.repository.EmployeeLeaveRepository;
import com.erp.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeLeaveService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeLeaveService.class);

    @Autowired
    private EmployeeRepository employeeRepository;


    
    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    
    public Page<EmployeeLeaveDto> getMyLeaves(Pageable pageable) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee employee = employeeRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with email: " + currentEmail));
        if (employee == null) {
            throw new UsernameNotFoundException("Employee not found with email: " + currentEmail);
        }
        return employeeLeaveRepository.findByEmployee(employee, pageable)
                .map(this::mapToDto);
    }



    // Create a leave request
    public EmployeeLeaveDto createLeaveRequest(EmployeeLeaveDto dto) {
        logger.info("Creating leave request for employee ID: {}", dto.getEmployeeId());

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> {
                    logger.error("Employee not found with ID: {}", dto.getEmployeeId());
                    return new EmployeeNotFoundException("Employee not found with ID: " + dto.getEmployeeId());
                });

        // Validate leave dates
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            logger.error("Invalid leave dates: Start date {} is after end date {}", dto.getStartDate(), dto.getEndDate());
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        // (Optional) Check for overlapping leaves if needed:
        // List<EmployeeLeave> overlaps = employeeLeaveRepository.findOverlappingLeaves(employee, dto.getStartDate(), dto.getEndDate());
        // if (!overlaps.isEmpty()) throw new IllegalStateException("Leave already exists in this period");

        // Map DTO to Entity
        EmployeeLeave leave = new EmployeeLeave();
        leave.setEmployee(employee);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setReason(dto.getReason());
        leave.setStatus(EmployeeLeave.LeaveStatus.PENDING);

        // Save and log
        EmployeeLeave savedLeave = employeeLeaveRepository.save(leave);
        logger.info("Leave request saved with ID: {}", savedLeave.getId());

        return mapToDto(savedLeave);
    }

    // Update leave status (e.g. Approve/Reject)
    public EmployeeLeaveDto updateLeaveStatus(Long id, String status) {
        logger.info("Updating leave status for ID: {} to {}", id, status);

        EmployeeLeave leave = employeeLeaveRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Leave request not found with ID: {}", id);
                    return new RuntimeException("Leave request not found with ID: " + id);
                });

        try {
            leave.setStatus(EmployeeLeave.LeaveStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid leave status: {}", status);
            throw new IllegalArgumentException("Invalid leave status: " + status);
        }

        EmployeeLeave updatedLeave = employeeLeaveRepository.save(leave);
        logger.info("Leave status updated for ID: {} to {}", id, updatedLeave.getStatus());

        return mapToDto(updatedLeave);
    }

    // Get all leaves paginated
    public Page<EmployeeLeaveDto> getAllLeaves(Pageable pageable) {
        logger.info("Fetching all leaves with pagination - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return employeeLeaveRepository.findAll(pageable).map(this::mapToDto);
    }

    // Map entity to DTO
    private EmployeeLeaveDto mapToDto(EmployeeLeave leave) {
        EmployeeLeaveDto dto = new EmployeeLeaveDto();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployee().getId());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus().toString());
        return dto;
    }
    
 // Fetch leave requests within a specific date range
    public List<EmployeeLeaveDto> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<EmployeeLeave> leaveRequests = employeeLeaveRepository.findByStartDateBetween(startDate, endDate);
        
        // Group by date and map to DTO
        Map<LocalDate, List<EmployeeLeave>> groupedByDate = leaveRequests.stream()
                .collect(Collectors.groupingBy(EmployeeLeave::getStartDate));

        // Flatten the grouped leaves to a list of DTOs
        return groupedByDate.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(this::mapToDto))
                .collect(Collectors.toList());
    }

    
}
