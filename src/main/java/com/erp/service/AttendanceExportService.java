package com.erp.service;

import com.erp.entity.Attendance;
import com.erp.repository.AttendanceRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceExportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // ✅ Excel Export Methods
    public ByteArrayInputStream exportAttendanceToExcel() {
        return buildExcel(attendanceRepository.findAll());
    }

    public ByteArrayInputStream exportAttendanceToExcel(LocalDate startDate, LocalDate endDate) {
        return buildExcel(attendanceRepository.findByDateBetween(startDate, endDate));
    }

    public ByteArrayInputStream exportAttendanceToExcelByEmployee(Long employeeId) {
        return buildExcel(attendanceRepository.findByEmployeeId(employeeId));
    }

    private ByteArrayInputStream buildExcel(List<Attendance> attendances) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance");

            Row header = sheet.createRow(0);
            createHeaderCell(header, 0, "Employee Name", workbook);
            createHeaderCell(header, 1, "Date", workbook);
            createHeaderCell(header, 2, "Status", workbook);

            int rowNum = 1;
            for (Attendance att : attendances) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(att.getEmployee().getName());
                row.createCell(1).setCellValue(att.getDate().toString());
                row.createCell(2).setCellValue(att.getStatus().toString());
            }

            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage(), e);
        }
    }

    private void createHeaderCell(Row row, int colIndex, String value, Workbook workbook) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    // ✅ PDF Export Methods
    public ByteArrayInputStream exportAttendanceToPDF() {
        return buildPDF(attendanceRepository.findAll());
    }

    public ByteArrayInputStream exportAttendanceToPDF(LocalDate startDate, LocalDate endDate) {
        return buildPDF(attendanceRepository.findByDateBetween(startDate, endDate));
    }

    public ByteArrayInputStream exportAttendanceToPDFByEmployee(Long employeeId) {
        return buildPDF(attendanceRepository.findByEmployeeId(employeeId));
    }

    private ByteArrayInputStream buildPDF(List<Attendance> attendances) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Paragraph title = new Paragraph("Employee Attendance Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16);
            document.add(title);

            float[] columnWidths = {3f, 3f, 2f};
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Employee Name").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Date").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Status").setBold()));

            for (Attendance att : attendances) {
                table.addCell(new Paragraph(att.getEmployee().getName()));
                table.addCell(new Paragraph(att.getDate().toString()));

                com.itextpdf.layout.element.Cell statusCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(att.getStatus().toString()));
                
                if ("ABSENT".equalsIgnoreCase(att.getStatus().toString())) {
                    statusCell.setFontColor(ColorConstants.RED);
                }
                table.addCell(statusCell);
            }


            document.add(table);
            document.close();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to export PDF: " + e.getMessage(), e);
        }
    }
}
