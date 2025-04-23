package com.erp.controller;

import com.erp.dto.AttendanceDto;
import com.erp.dto.AttendanceResponseDto;
import com.erp.dto.AttendanceSummaryDto;
import com.erp.dto.DailyAttendanceDto;
import com.erp.dto.DepartmentAttendanceDto;
import com.erp.dto.WeeklyAttendanceDto;
import com.erp.entity.Attendance;
import com.erp.repository.LeaveRepository;
import com.erp.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.colors.ColorConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private LeaveRepository leaveRepository;


    @PostMapping
    public ResponseEntity<?> markAttendance(@RequestBody AttendanceDto attendanceDto) {
        try {
            Attendance attendance = attendanceService.markAttendance(attendanceDto);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/mark-now/{employeeId}")
    public ResponseEntity<String> markAttendanceNow(@PathVariable Long employeeId) {
        boolean marked = attendanceService.markAttendanceNow(employeeId);
        return marked
            ? ResponseEntity.ok("Attendance marked successfully.")
            : ResponseEntity.status(HttpStatus.CONFLICT).body("Attendance already marked for today.");
    }


    @GetMapping("/employee/{id}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByEmployee(@PathVariable Long id) {
        List<Attendance> records = attendanceService.getAttendanceByEmployeeId(id);
        List<AttendanceDto> response = records.stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/monthly-report/{employeeId}/pdf")
    public ResponseEntity<byte[]> exportMonthlyReportToPdf(@PathVariable Long employeeId,
                                                            @RequestParam int month,
                                                            @RequestParam int year) throws IOException {
        List<DailyAttendanceDto> report = attendanceService.getMonthlyAttendanceDetails(employeeId, month, year);
        ByteArrayOutputStream pdfStream = attendanceService.exportToPdf(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfStream.toByteArray());
    }

    @GetMapping("/monthly-report/{employeeId}/excel")
    public ResponseEntity<byte[]> exportMonthlyReportToExcel(@PathVariable Long employeeId,
                                                              @RequestParam int month,
                                                              @RequestParam int year) throws IOException {
        List<DailyAttendanceDto> report = attendanceService.getMonthlyAttendanceDetails(employeeId, month, year);
        ByteArrayOutputStream byteArrayOutputStream = attendanceService.exportToExcel(report);

        byte[] data = byteArrayOutputStream.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/dashboard/{employeeId}")
    public ResponseEntity<Map<String, Object>> getDashboardData(@PathVariable Long employeeId) {
        Map<String, Object> dashboard = attendanceService.getEmployeeDashboardData(employeeId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/employee/{id}/page")
    public ResponseEntity<Page<AttendanceResponseDto>> getEmployeeAttendancePaginated(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        // Ensure page and size are non-negative
        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> attendancePage = attendanceService.getPaginatedAttendanceForEmployee(id, pageable);

        // If no attendance data is found, return 404
        if (attendancePage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Convert Attendance to DTO
        Page<AttendanceResponseDto> dtoPage = attendancePage.map(attendanceService::convertToDto);
        
        return ResponseEntity.ok(dtoPage);
    }



    @GetMapping("/summary/{employeeId}")
    public ResponseEntity<Map<String, Integer>> getMonthlyAttendanceSummary(@PathVariable Long employeeId,
                                                                            @RequestParam int month,
                                                                            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMonthlyAttendanceSummary(employeeId, month, year));
    }

    @GetMapping("/range")
    public ResponseEntity<List<Attendance>> getAttendanceByDateRange(@RequestParam LocalDate start,
                                                                    @RequestParam LocalDate end) {
        return ResponseEntity.ok(attendanceService.getAttendanceBetweenDates(start, end));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    @GetMapping("/all/page")
    public ResponseEntity<Page<Attendance>> getAllAttendancePaginated(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> attendance = attendanceService.getAllPaginatedAttendance(pageable);
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/attendance/filter")
    public Page<AttendanceResponseDto> getFilteredAttendance(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable) {

        // Validate or preprocess parameters if needed
        return attendanceService.getFilteredAttendance(startDate, endDate, employeeId, status, pageable);
    }

    
    @GetMapping("/summary/monthly")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<AttendanceSummaryDto> getMonthlySummary(@RequestParam("month") String monthStr) {
        return ResponseEntity.ok(attendanceService.getMonthlyAttendanceSummary(monthStr));
    }
    
    @GetMapping("/summary/weekly")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<WeeklyAttendanceDto> getWeeklySummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(attendanceService.getWeeklyAttendanceSummary(start, end));
    }

    
//    @GetMapping("/summary/department")
//    @PreAuthorize("hasRole('HR')")
//    public ResponseEntity<DepartmentAttendanceDto> getDepartmentSummary(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        return ResponseEntity.ok(attendanceService.getDepartmentWiseAttendance(date));
//    }


}

