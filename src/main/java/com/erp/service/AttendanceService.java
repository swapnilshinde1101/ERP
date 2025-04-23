  package com.erp.service;

import com.erp.dto.AttendanceDto;
import com.erp.dto.AttendanceResponseDto;
import com.erp.dto.AttendanceSummaryDto;
import com.erp.dto.DailyAttendanceDto;
import com.erp.dto.DepartmentAttendanceDto;
import com.erp.dto.WeeklyAttendanceDto;
import com.erp.entity.Attendance;
import com.erp.entity.Attendance.AttendanceStatus;
import com.erp.entity.Employee;
import com.erp.entity.Leave.LeaveStatus;
import com.erp.exception.InvalidStatusException;
import com.erp.repository.AttendanceRepository;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.LeaveRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.stream.Collectors;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;


@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;


    public Attendance markAttendance(AttendanceDto attendanceDto) {
        Employee employee = employeeRepository.findById(attendanceDto.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        LocalDate attendanceDate = attendanceDto.getDate() != null ? attendanceDto.getDate() : LocalDate.now();

        if (attendanceRepository.existsByEmployeeIdAndDate(employee.getId(), attendanceDate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance already marked for " + attendanceDate);
        }

        Attendance attendance = attendanceDto.toEntity(employee);

        return attendanceRepository.save(attendance);
    }

    public boolean markAttendanceNow(Long employeeId) {
        LocalDate today = LocalDate.now();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, today)) {
            return false; // Already marked
        }

        boolean isOnLeave = isOnApprovedLeave(employeeId, today);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .status(isOnLeave ? AttendanceStatus.LEAVE : AttendanceStatus.PRESENT)
                .present(!isOnLeave)
                .remarks(isOnLeave ? "On Approved Leave" : null)
                .overtime(false)
                .build();

        attendanceRepository.save(attendance);
        return true;
    }

    private boolean isOnApprovedLeave(Long employeeId, LocalDate date) {
        return leaveRepository.existsByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                employeeId, date, date, LeaveStatus.APPROVED
        );
    }



    public List<Attendance> getAttendanceForEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public List<Attendance> getAttendanceBetweenDates(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByDateBetween(startDate, endDate);
    }

    public Page<Attendance> getPaginatedAttendanceForEmployee(Long employeeId, Pageable pageable) {
        return attendanceRepository.findByEmployeeId(employeeId, pageable);
    }

    public List<Attendance> getAttendanceByEmployeeId(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public Page<Attendance> getAllPaginatedAttendance(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    public Map<String, Object> getEmployeeDashboardData(Long employeeId) {
        Map<String, Object> dashboard = new HashMap<>();
        List<Attendance> attendanceRecords = attendanceRepository.findByEmployeeId(employeeId);

        if (attendanceRecords == null || attendanceRecords.isEmpty()) {
            dashboard.put("message", "No attendance data available for this employee.");
            return dashboard;
        }

        long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
        long absentCount = attendanceRecords.size() - presentCount;

        dashboard.put("presentDays", presentCount);
        dashboard.put("absentDays", absentCount);
        return dashboard;
    }


    public List<DailyAttendanceDto> getMonthlyAttendanceDetails(Long employeeId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Attendance> records = attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);

        Map<LocalDate, String> attendanceMap = records.stream()
                .collect(Collectors.toMap(Attendance::getDate, a -> {
                    if (a.getStatus() == AttendanceStatus.LEAVE) return "Leave";
                    else return a.isPresent() ? "Present" : "Absent";
                }));


        List<DailyAttendanceDto> report = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            report.add(new DailyAttendanceDto(date, attendanceMap.getOrDefault(date, "Not Marked")));
        }

        return report;
    }

 // Method to export the attendance report to PDF
    public ByteArrayOutputStream exportToPdf(List<DailyAttendanceDto> report) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
            Document document = new Document(pdfDocument);

            Table table = new Table(new float[]{1, 1});
            table.setWidth(UnitValue.createPercentValue(100));

            table.addCell(new Cell().add(new Paragraph("Date")));
            table.addCell(new Cell().add(new Paragraph("Status")));

            for (DailyAttendanceDto dto : report) {
                table.addCell(new Cell().add(new Paragraph(dto.getDate().toString())));
                table.addCell(new Cell().add(new Paragraph(dto.getStatus())));
            }

            document.add(table);
            document.close();

            return outputStream;
        }
    }


    // Method to export the attendance report to Excel
    public ByteArrayOutputStream exportToExcel(List<DailyAttendanceDto> report) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();  // or HSSFWorkbook for .xls files
        Sheet sheet = workbook.createSheet("Attendance Report");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Date");
        headerRow.createCell(1).setCellValue("Status");

        int rowNum = 1;
        for (DailyAttendanceDto dto : report) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getDate().toString());
            row.createCell(1).setCellValue(dto.getStatus());
        }

        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }


    public Map<String, Integer> getMonthlyAttendanceSummary(Long employeeId, int month, int year) {
        List<DailyAttendanceDto> attendanceDetails = getMonthlyAttendanceDetails(employeeId, month, year);

        // Directly count statuses during the iteration
        Map<String, Integer> summary = new HashMap<>();
        summary.put("Present", 0);
        summary.put("Absent", 0);
        summary.put("Leave", 0);

        attendanceDetails.forEach(dto -> {
            summary.put(dto.getStatus(), summary.get(dto.getStatus()) + 1);
        });

        return summary;
    }



 // Method to get all attendance records
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }
    
 // Method to convert Attendance entity to AttendanceResponseDto
 // Method to convert Attendance entity to AttendanceResponseDto
    public AttendanceResponseDto convertToDto(Attendance attendance) {
        if (attendance == null) {
            return null;  // or throw an exception, depending on your use case
        }
        return new AttendanceResponseDto(
                attendance.getEmployee().getId(),
                attendance.getEmployee().getName(),
                attendance.getDate(),
                attendance.getStatus().toString(),
                attendance.isPresent()
        );
    }

    
    

    public Page<AttendanceResponseDto> getFilteredAttendance(LocalDate startDate, LocalDate endDate, Long employeeId, String status, Pageable pageable) {
        Page<Attendance> attendancePage;

        try {
            AttendanceStatus attendanceStatus = status != null ? AttendanceStatus.valueOf(status) : null;
            if (startDate != null && endDate != null) {
                attendancePage = attendanceRepository.findByDateBetweenAndEmployeeIdAndStatus(startDate, endDate, employeeId, attendanceStatus, pageable);
            } else if (employeeId != null) {
                attendancePage = attendanceRepository.findByEmployeeIdAndStatus(employeeId, attendanceStatus, pageable);
            } else if (status != null) {
                attendancePage = attendanceRepository.findByStatus(attendanceStatus, pageable);
            } else {
                attendancePage = attendanceRepository.findAll(pageable);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid status value provided");
        }

        return attendancePage.map(AttendanceResponseDto::fromEntity);
    }


    
    public Attendance saveAttendance(AttendanceDto dto) {
        LocalDate date = dto.getDate();
        Long empId = dto.getEmployeeId();

        // ✅ Check if leave exists and is approved for the date
        boolean isOnLeave = leaveRepository.existsByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            empId, date, date, LeaveStatus.APPROVED
        );

        if (isOnLeave) {
            throw new InvalidStatusException("Attendance cannot be marked. Employee is on approved leave.");
        }

        // Checking if the status provided is valid
        if (dto.getStatus() == null || (!dto.getStatus().equals("PRESENT") && !dto.getStatus().equals("ABSENT"))) {
            throw new InvalidStatusException("Invalid attendance status provided.");
        }

        // Save normal attendance
        Attendance attendance = Attendance.builder()
                .employee(employeeRepository.findById(empId).orElseThrow())
                .date(date)
                .status(dto.isPresent() ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT)
                .present(dto.isPresent())
                .build();

        return attendanceRepository.save(attendance);
    }



    public AttendanceSummaryDto getMonthlyAttendanceSummary(String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr); // e.g., "2025-04"
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Attendance> allRecords = attendanceRepository.findByDateBetween(start, end);

        long present = allRecords.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = allRecords.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long leave = allRecords.stream().filter(a -> a.getStatus() == AttendanceStatus.LEAVE).count();
        long notMarked = allRecords.stream().filter(a -> a.getStatus() == AttendanceStatus.NOT_MARKED).count();

        return new AttendanceSummaryDto(present, absent, leave, notMarked);
    }


    
    
    public WeeklyAttendanceDto getWeeklyAttendanceSummary(LocalDate start, LocalDate end) {
        List<Attendance> records = attendanceRepository.findByDateBetween(start, end);

        Map<String, Map<String, Long>> result = records.stream()
            .collect(Collectors.groupingBy(
                a -> a.getDate().toString(),
                Collectors.groupingBy(
                    a -> a.getStatus().toString(),
                    Collectors.counting()
                )
            ));

        return new WeeklyAttendanceDto(result);
    }

    
//    public DepartmentAttendanceDto getDepartmentWiseAttendance(LocalDate date) {
//        List<Attendance> records = attendanceRepository.findByDate(date);
//
//        Map<String, Map<String, Long>> result = records.stream()
//                .filter(a -> a.getEmployee() != null && a.getEmployee().getDepartment() != null)
//                .collect(Collectors.groupingBy(
//                        a -> a.getEmployee().getDepartment().getName(),
//                        Collectors.groupingBy(
//                                a -> a.getStatus().name(),
//                                Collectors.counting()
//                        )
//                ));
//
//        DepartmentAttendanceDto dto = new DepartmentAttendanceDto();
//        dto.setDepartmentStatus(result); // or setDepartmentAttendance(result) if you're using that field
//        return dto;
//    }








    

}