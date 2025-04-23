package com.erp.controller;

import com.erp.service.AttendanceExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance/export")
public class AttendanceExportController {

    @Autowired
    private AttendanceExportService exportService;

    // ✅ Export All to Excel (or with optional filters)
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {
        ByteArrayInputStream in;
        if (employeeId != null) {
            in = exportService.exportAttendanceToExcelByEmployee(employeeId);
        } else if (startDate != null && endDate != null) {
            in = exportService.exportAttendanceToExcel(startDate, endDate);
        } else {
            in = exportService.exportAttendanceToExcel();
        }

        return buildResponse(in, "attendance.xlsx", MediaType.APPLICATION_OCTET_STREAM);
    }

    // ✅ Export All to PDF (or with optional filters)
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {
        ByteArrayInputStream in;
        if (employeeId != null) {
            in = exportService.exportAttendanceToPDFByEmployee(employeeId);
        } else if (startDate != null && endDate != null) {
            in = exportService.exportAttendanceToPDF(startDate, endDate);
        } else {
            in = exportService.exportAttendanceToPDF();
        }

        return buildResponse(in, "attendance.pdf", MediaType.APPLICATION_PDF);
    }

    // ✅ Reusable response method
    private ResponseEntity<byte[]> buildResponse(ByteArrayInputStream stream, String filename, MediaType mediaType) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(stream.readAllBytes());
    }
}
