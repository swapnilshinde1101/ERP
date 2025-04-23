// EmployeeController.java
package com.erp.controller;

import com.erp.dto.EmployeeDto;
import com.erp.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('INVENTORY') or hasRole('FINANCE')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @GetMapping("/paged")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployeesPaged(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(employeeService.getAllEmployeesPaged(pageable));
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }

  @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<String> toggleEmployeeStatus(@PathVariable Long id) {
        boolean isActive = employeeService.toggleEmployeeStatus(id);
        String message = isActive ? "Employee activated successfully" : "Employee deactivated successfully";
        return ResponseEntity.ok(message);
    }




  @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
  @GetMapping("/filter")
  public ResponseEntity<Page<EmployeeDto>> getByRoleAndDepartment(@RequestParam String role,
                                                                   @RequestParam String department,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
      Pageable pageable = PageRequest.of(page, size);
      return ResponseEntity.ok(employeeService.getByRoleAndDepartment(role, department, pageable));
  }



    
    
}
