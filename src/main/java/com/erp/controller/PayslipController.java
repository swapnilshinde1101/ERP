package com.erp.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.erp.Utility.PdfGeneratorUtil;
import com.erp.dto.MyPayslipDto;
import com.erp.dto.PayslipLogDto;
import com.erp.dto.SalaryPayslipDto;
import com.erp.service.EmailService;
import com.erp.service.PayslipService;
import com.erp.service.SalaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payslip")
@RequiredArgsConstructor
public class PayslipController {
	
	private final SalaryService salaryService;
    private final EmailService emailService;
    private final PayslipService payslipService;
    
    // Get payslip by ID
    @GetMapping("/payslip/{id}")
    @PreAuthorize("hasAnyRole('HR', 'FINANCE')")
    public ResponseEntity<SalaryPayslipDto> getPayslip(@PathVariable Long id) {
        return ResponseEntity.ok(payslipService.getPayslipById(id));
    }

    
    // Download PDF of payslip
    @GetMapping("/payslip/{id}/download")
    @PreAuthorize("hasAnyRole('HR', 'FINANCE')")
    public ResponseEntity<byte[]> downloadPayslipPdf(@PathVariable Long id) throws MalformedURLException {
        SalaryPayslipDto dto = payslipService.getPayslipById(id);
        byte[] pdf = PdfGeneratorUtil.generatePayslipPdf(dto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Payslip_" + dto.getEmployeeName() + ".pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    // Email payslip to employee
    @PostMapping("/payslip/{id}/email")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> emailPayslip(@PathVariable Long id) {
        try {
            // Retrieve the payslip DTO by ID
            SalaryPayslipDto dto = payslipService.getPayslipById(id);
            
            // Generate the payslip PDF from the DTO
            byte[] pdf = PdfGeneratorUtil.generatePayslipPdf(dto);
            
            // Retrieve the employee email
            String email = salaryService.getEmployeeEmailBySalaryId(id);
            
            // Send the email with the payslip attached
            emailService.sendPayslip(email, pdf, dto.getEmployeeName());

            // Return a success message
            return ResponseEntity.ok("Payslip emailed to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
        
        
    }
    
   
    
 // Filter payslips by month, status, employee
    @GetMapping("/payslip/filter")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<SalaryPayslipDto>> filterPayslips(
            @RequestParam("month") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam(value = "status", required = false) Boolean isPaid,
            @RequestParam(value = "employeeName", required = false) String employeeName) {
        return ResponseEntity.ok(payslipService.filterPayslips(month, isPaid, employeeName));
    }

    
    // Download payslip (for employee or admin)
    @GetMapping("/payslip/{id}/download/self")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR', 'ADMIN')")
    public ResponseEntity<byte[]> downloadPayslip(
            @PathVariable Long id,
            Principal principal) {
        byte[] pdfBytes = payslipService.getPayslipPdf(id, principal.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("Payslip_" + id + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // My salary (for employee)
    @GetMapping("/my-salary")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<MyPayslipDto>> getMyPayslips(Principal principal) {
        // Get email from the Principal object
        String email = principal.getName();
        
        // Call the service method to fetch the payslips
        List<MyPayslipDto> payslips = salaryService.getMyPayslips(email);
        
        // Return the payslips in the response
        return ResponseEntity.ok(payslips);
    }


    @GetMapping("/logs/payslip/{payslipId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<PayslipLogDto>> getLogsForPayslip(@PathVariable Long payslipId) {
        // Call the service to get the logs for the given payslipId
        List<PayslipLogDto> logs = salaryService.getLogsByPayslipId(payslipId);

        // If no logs are found, return a 404 Not Found
        if (logs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        // Return the logs in the response
        return ResponseEntity.ok(logs);
    }


    // Payslip logs by employeeId
    @GetMapping("/logs/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<PayslipLogDto>> getLogsForEmployee(@PathVariable Long employeeId) {
        // Call the service to get the logs for the given employeeId
        List<PayslipLogDto> logs = salaryService.getLogsByEmployee(employeeId);

        // If no logs are found, return a 404 Not Found with an empty list
        if (logs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        // Return the logs in the response
        return ResponseEntity.ok(logs);
    }


    // Filtered logs by payslipId
    @GetMapping("/logs/payslip/{payslipId}/filter")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<PayslipLogDto>> getFilteredLogsForPayslip(
            @PathVariable Long payslipId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String actionType) {
        return ResponseEntity.ok(salaryService.getFilteredLogs(payslipId, month, year, actionType));
    }

    // Filtered logs by employeeId
//    @GetMapping("/logs/employee/{employeeId}/filter")
//    @PreAuthorize("hasAnyRole('HR', 'ADMIN')") // Removed SpEL for principal.id for safety
//    public ResponseEntity<List<PayslipLogDto>> getFilteredLogsForEmployee(
//            @PathVariable Long employeeId,
//            @RequestParam(required = false) Integer month,
//            @RequestParam(required = false) Integer year,
//            @RequestParam(required = false) String actionType) {
//        return ResponseEntity.ok(salaryService.getFilteredLogsByEmployee(employeeId, month, year, actionType));
//    }
//    
//    @GetMapping("/payslip/download/all")
//    @PreAuthorize("hasAnyRole('FINANCE', 'ADMIN')")
//    public ResponseEntity<?> downloadAllPayslips(
//            @RequestParam(name = "month", required = false)
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) throws IOException {
//
//        if (month == null) {
//            return ResponseEntity.badRequest().body("Month parameter is required in format: YYYY-MM-DD");
//        }
//
//        byte[] zipBytes = payslipService.downloadAllPayslipsAsZip(month);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setContentDisposition(ContentDisposition.builder("attachment")
//                .filename("Payslips_" + month.getMonth() + ".zip").build());
//
//        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
//    }


    
  

}
