package com.erp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.erp.dto.DepartmentDTO;
import com.erp.entity.Department;
import com.erp.service.DepartmentService;
import com.erp.exception.DepartmentNotFoundException;
import com.erp.response.ApiResponse;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> createDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        departmentService.createDepartment(departmentDTO);
        return new ResponseEntity<>(new ApiResponse("Department created successfully", true), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return new ResponseEntity<>(departments, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return new ResponseEntity<>(department, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateDepartment(@PathVariable Long id,
                                                        @Valid @RequestBody DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(id, departmentDTO);
        return new ResponseEntity<>(new ApiResponse("Department updated successfully", true), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return new ResponseEntity<>(new ApiResponse("Department deleted successfully", true), HttpStatus.OK);
    }
}
