package com.erp.service;

import com.erp.entity.Payslip;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    public byte[] generatePayslipPdf(Payslip payslip) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph title = new Paragraph("SALARY PAYSLIP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Spacer

            // Metadata Info Table (Month, Year, Date Issued, Status)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingBefore(10);
            metaTable.setSpacingAfter(10);

            metaTable.addCell(getCell("Month:", PdfPCell.ALIGN_LEFT, true));
            metaTable.addCell(getCell(payslip.getMonth() + " " + payslip.getYear(), PdfPCell.ALIGN_LEFT, false));
            metaTable.addCell(getCell("Date Issued:", PdfPCell.ALIGN_LEFT, true));
            metaTable.addCell(getCell(payslip.getDateIssued().toString(), PdfPCell.ALIGN_LEFT, false));
            metaTable.addCell(getCell("Payslip Status:", PdfPCell.ALIGN_LEFT, true));
            metaTable.addCell(getCell(payslip.getStatus(), PdfPCell.ALIGN_LEFT, false));

            document.add(metaTable);

            // Employee Info Table
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("Employee Details", sectionTitleFont));
            document.add(new Paragraph(" "));

            PdfPTable empTable = new PdfPTable(2);
            empTable.setWidthPercentage(100);
            empTable.setSpacingBefore(5);
            empTable.setSpacingAfter(10);

            empTable.addCell(getCell("Employee Name:", PdfPCell.ALIGN_LEFT, true));
            empTable.addCell(getCell(payslip.getEmployee().getName(), PdfPCell.ALIGN_LEFT, false));
            empTable.addCell(getCell("Employee ID:", PdfPCell.ALIGN_LEFT, true));
            empTable.addCell(getCell(String.valueOf(payslip.getEmployee().getId()), PdfPCell.ALIGN_LEFT, false));
            empTable.addCell(getCell("Department:", PdfPCell.ALIGN_LEFT, true));
            empTable.addCell(getCell(payslip.getEmployee().getDepartment().getName(), PdfPCell.ALIGN_LEFT, false));

            document.add(empTable);

            // Salary Info
            document.add(new Paragraph("Salary Breakdown", sectionTitleFont));
            document.add(new Paragraph(" "));

            PdfPTable salaryTable = new PdfPTable(2);
            salaryTable.setWidthPercentage(100);
            salaryTable.setSpacingBefore(5);
            salaryTable.setSpacingAfter(10);

            salaryTable.addCell(getCell("Base Salary:", PdfPCell.ALIGN_LEFT, true));
            salaryTable.addCell(getCell("₹" + payslip.getBaseSalary(), PdfPCell.ALIGN_LEFT, false));
            salaryTable.addCell(getCell("Bonus:", PdfPCell.ALIGN_LEFT, true));
            salaryTable.addCell(getCell("₹" + (payslip.getBonus() != null ? payslip.getBonus() : 0.0), PdfPCell.ALIGN_LEFT, false));
            salaryTable.addCell(getCell("Deductions:", PdfPCell.ALIGN_LEFT, true));
            salaryTable.addCell(getCell("₹" + (payslip.getDeduction() != null ? payslip.getDeduction() : 0.0), PdfPCell.ALIGN_LEFT, false));
            salaryTable.addCell(getCell("Net Salary:", PdfPCell.ALIGN_LEFT, true));
            salaryTable.addCell(getCell("₹" + payslip.getNetSalary(), PdfPCell.ALIGN_LEFT, false, new BaseColor(0, 102, 0)));

            document.add(salaryTable);

            // Footer
            Paragraph footer = new Paragraph("This is a system-generated document. No signature is required.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // Utility method to style table cells
    private PdfPCell getCell(String text, int alignment, boolean isBold) {
        return getCell(text, alignment, isBold, BaseColor.BLACK);
    }

    private PdfPCell getCell(String text, int alignment, boolean isBold, BaseColor color) {
        Font font = isBold
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, color)
                : FontFactory.getFont(FontFactory.HELVETICA, 12, color);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}
