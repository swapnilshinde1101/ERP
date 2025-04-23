package com.erp.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTestEmail(String to) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);

        helper.setTo(to);
        helper.setSubject("Test Email from ERP");
        helper.setText("Hello! This is a test email from the ERP system.");

        mailSender.send(message);
    }

    public void sendPayslip(String to, byte[] pdf, String employeeName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String safeName = employeeName.trim().replaceAll("[^a-zA-Z0-9]", "_");

            helper.setTo(to);
            helper.setFrom("noreply@yourcompany.com", "ERP System");
            helper.setSubject("Your Monthly Payslip - " + employeeName);

            helper.setText(
                "<p>Dear <b>" + employeeName + "</b>,</p>" +
                "<p>Your monthly payslip is attached below.</p>" +
                "<p><i>This is a system-generated document. No signature is required.</i></p>" +
                "<p>Regards,<br><b>ERP Payroll Team</b></p>",
                true
            );

            ByteArrayResource resource = new ByteArrayResource(pdf);
            helper.addAttachment("Payslip_" + safeName + ".pdf", resource);

            mailSender.send(message);
            System.out.println("✅ Payslip email sent successfully to: " + to);

        } catch (Exception e) {
            System.err.println("❌ Error sending payslip to: " + to);
            e.printStackTrace();
        }
    }

}
