package com.erp.Utility;

import com.erp.dto.SalaryPayslipDto;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;

public class PdfGeneratorUtil {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₹#,##0.00");

    public static byte[] generatePayslipPdf(SalaryPayslipDto payslip) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Logo (optional)
        try {
            InputStream logoStream = PdfGeneratorUtil.class.getResourceAsStream("/static/logo.png"); // place logo in resources/static
            if (logoStream != null) {
                Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()));
                logo.setWidth(100);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
            document.add(new Paragraph("Company Name").setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        }

        // Title
        document.add(new Paragraph("Salary Payslip")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Employee Info
        document.add(new Paragraph("Employee Name: " + payslip.getEmployeeName()));
        document.add(new Paragraph("Department: " + payslip.getDepartment()));
        document.add(new Paragraph("Month: " + payslip.getMonth()));
        document.add(new Paragraph("Status: " + (payslip.isPaid() ? "Paid" : "Pending")));
        document.add(new Paragraph("\n"));

        // Salary Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

        table.addCell(getCell("Base Salary", true));
        table.addCell(getCell(CURRENCY_FORMAT.format(payslip.getBaseSalary()), false));
        table.addCell(getCell("Bonus", true));
        table.addCell(getCell(CURRENCY_FORMAT.format(payslip.getBonus()), false));
        table.addCell(getCell("Tax", true));
        table.addCell(getCell(CURRENCY_FORMAT.format(payslip.getTax()), false));
        table.addCell(getCell("Deductions", true));
        table.addCell(getCell(CURRENCY_FORMAT.format(payslip.getDeduction()), false));
        table.addCell(getCell("Total Earnings", true));
        table.addCell(getCell(CURRENCY_FORMAT.format(payslip.getTotalEarnings()), false));

        document.add(table);

        document.close();
        return outputStream.toByteArray();
    }

    private static Cell getCell(String text, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text));
        if (isHeader) {
            cell.setBold();
        }
        return cell;
    }
}
