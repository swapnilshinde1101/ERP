package com.erp.service;

import com.erp.Utility.PdfGeneratorUtil;
import com.erp.dto.SalaryPayslipDto;

import jakarta.mail.MessagingException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

	
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PayslipSchedulerService {

	
    private final PayslipService payslipService;
    private final SalaryService salaryService;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(PayslipSchedulerService.class);

    public PayslipSchedulerService(SalaryService salaryService, EmailService emailService,PayslipService payslipService )
    {
        this.salaryService = salaryService;
        this.emailService = emailService;
        this.payslipService = payslipService;
    }

     // Runs at 00:00 on the 1st day of every month
    @Scheduled(cron = "0 0 0 1 * ?", zone = "Asia/Kolkata")
    public void generateAndEmailPayslips() throws MessagingException {
        LocalDate currentMonth = LocalDate.now().minusMonths(1);

        List<SalaryPayslipDto> payslips = payslipService.generatePayslipsForMonth(currentMonth);

        int success = 0;
        int failed = 0;

        for (SalaryPayslipDto payslip : payslips) {
            try {
                byte[] pdf = PdfGeneratorUtil.generatePayslipPdf(payslip);
                emailService.sendPayslip(payslip.getEmployeeEmail(), pdf, payslip.getEmployeeName());
                log.info("✅ Payslip sent to: {}", payslip.getEmployeeEmail());
                success++;
            } catch (Exception e) {
                log.error("❌ Error occurred while processing payslip for: {}", payslip.getEmployeeEmail(), e);
                failed++;
            }
        }
        log.info("📨 Payslip Summary - Total: {}, Sent: {}, Failed: {}", payslips.size(), success, failed);

    }
}
