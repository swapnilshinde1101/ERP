package com.erp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.erp.dto.SalaryPayslipDto;
import com.erp.entity.Employee;
import com.erp.entity.Payslip;
import com.erp.entity.Salary;
import com.erp.exception.EmployeeNotFoundException;
import com.erp.exception.PayslipNotFoundException;
import com.erp.exception.PayslipZipGenerationException;
import com.erp.exception.SalaryNotFoundException;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.PayslipRepository;
import com.erp.repository.SalaryRepository;

@Service
public class PayslipService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private PdfGeneratorService pdfGenerator;

    public Payslip generatePayslip(Long employeeId, double baseSalary, double bonus, double deduction) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + employeeId));

        Payslip payslip = new Payslip();
        payslip.setEmployee(employee);
        payslip.setBaseSalary(baseSalary);
        payslip.setBonus(bonus);
        payslip.setDeduction(deduction);
        payslip.calculateNetSalary();
        payslip.setDateIssued(LocalDate.now());
        payslip.setStatus("Generated");
        payslip.setDownloadUrl("/api/payslip/download/" + payslip.getId());

        return payslipRepository.save(payslip);
    }

    public byte[] getPayslipPdf(Long payslipId, String username) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new PayslipNotFoundException("Payslip not found with ID: " + payslipId));

        if (!payslip.getEmployee().getEmail().equals(username) && !hasHRorAdminAccess()) {
            throw new AccessDeniedException("Unauthorized access to payslip");
        }

        return pdfGenerator.generatePayslipPdf(payslip);
    }

    private boolean hasHRorAdminAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("HR") || role.getAuthority().equals("ADMIN"));
    }

//    public byte[] downloadAllPayslipsAsZip(LocalDate month) {
//        List<Salary> salaries = salaryRepository.findByMonthAndYear(month.getMonthValue(), month.getYear());
//
//
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//             ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
//
//            for (Salary salary : salaries) {
//                Payslip payslip = new Payslip();
//                payslip.setEmployee(salary.getEmployee());
//                payslip.setBaseSalary(salary.getBaseSalary());
//                payslip.setBonus(salary.getBonus());
//                payslip.setDeduction(salary.getDeduction());
//                payslip.setTotalPayable(salary.getTotalPayable());
//                payslip.setDateIssued(LocalDate.now());
//                payslip.setStatus("Generated");
//
//                String employeeName = (salary.getEmployee() != null ? salary.getEmployee().getEmployeeName() : "Unknown")
//                        .replaceAll("[^a-zA-Z0-9_-]", "_");
//
//                String formattedMonth = month.format(DateTimeFormatter.ofPattern("MMMM-yyyy"));
//                String fileName = "Payslip_" + employeeName + "_" + formattedMonth + ".pdf";
//
//                zipOut.putNextEntry(new ZipEntry(fileName));
//                byte[] payslipPdf = pdfGenerator.generatePayslipPdf(payslip);
//                zipOut.write(payslipPdf);
//                zipOut.closeEntry();
//            }
//
//            return byteArrayOutputStream.toByteArray();
//        } catch (IOException e) {
//            throw new PayslipZipGenerationException("Failed to generate ZIP of payslips", e);
//        }
//    }

    public List<SalaryPayslipDto> generatePayslipsForMonth(LocalDate month) {
        return new ArrayList<>(); // To be implemented if needed
    }

    public SalaryPayslipDto getPayslipById(Long id) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new SalaryNotFoundException("Salary not found with ID: " + id));

        Employee emp = salary.getEmployee();
        if (emp == null) {
            throw new RuntimeException("Employee data is missing for salary ID: " + id);
        }

        return SalaryPayslipDto.builder()
                .id(salary.getId())
                .employeeName(emp.getName())
                .department(emp.getDepartment().getName())
                .month(salary.getMonth().toString())
                .baseSalary(salary.getBaseSalary())
                .presentDays(salary.getPresentDays())
                .absentDays(salary.getAbsentDays())
                .leaveDays(salary.getLeaveDays())
                .totalEarnings(salary.getTotalEarnings())
                .approvedByHR(salary.isApprovedByHR())
                .forwardedToFinance(salary.isForwardedToFinance())
                .paid(salary.isPaid())
                .build();
    }

    public List<SalaryPayslipDto> filterPayslipsByMonthAndStatus(LocalDate month, Boolean isPaid) {
        if (month == null || isPaid == null) {
            throw new IllegalArgumentException("Month and payment status must be provided.");
        }

        List<Salary> salaries = salaryRepository.findByMonthAndApprovedByHR(month, isPaid);

        return salaries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SalaryPayslipDto mapToDto(Salary salary) {
        Employee emp = salary.getEmployee();

        return SalaryPayslipDto.builder()
                .id(salary.getId())
                .employeeName(salary.getEmployeeName())
                .employeeEmail(emp != null ? emp.getEmail() : "")
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
                .netSalary(salary.getTotalEarnings())

                .approvedByHR(salary.isApprovedByHR())
                .forwardedToFinance(salary.isForwardedToFinance())
                .paid(salary.isPaid())

                .status(salary.getStatus())
                .downloadUrl("/api/salary/download/" + salary.getId())
                .build();
    }

    public List<SalaryPayslipDto> filterPayslips(LocalDate month, Boolean isPaid, String employeeName) {
        List<Salary> salaries = salaryRepository.findByMonthAndApprovedByHRAndEmployeeName(month, isPaid, employeeName);

        return salaries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

}
