// EmployeeService.java
package com.erp.service;

import com.erp.dto.EmployeeDto;
import com.erp.entity.Department;
import com.erp.entity.Employee;
import com.erp.entity.USER_ROLE;
import com.erp.exception.EmployeeNotFoundException;
import com.erp.repository.DepartmentRepository;
import com.erp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

	private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository; 


    // Convert Employee entity to DTO
    private EmployeeDto mapToDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .role(employee.getRole().name())
                .departmentId(employee.getDepartment().getId())  // Convert Department entity to id
                .departmentName(employee.getDepartment().getName())  // Optional: Store department name if needed
                .baseSalary(employee.getBaseSalary())
                .bonus(employee.getBonus())
                .deduction(employee.getDeduction())
                .performanceRating(employee.getPerformanceRating() != null ? String.valueOf(employee.getPerformanceRating()) : null)
                .active(employee.getIsActive())
                .password(employee.getPassword())
                .build();
    }

    // Convert DTO to Employee entity
    private Employee mapToEntity(EmployeeDto dto) {
        Department department;

        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + dto.getDepartmentId()));
        } else if (dto.getDepartmentName() != null && !dto.getDepartmentName().isBlank()) {
            department = departmentRepository.findByNameIgnoreCase(dto.getDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found with name: " + dto.getDepartmentName()));
        } else {
            throw new RuntimeException("Either departmentId or departmentName must be provided.");
        }

        return Employee.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(USER_ROLE.valueOf(dto.getRole().toUpperCase()))
                .department(department)
                .baseSalary(dto.getBaseSalary())
                .bonus(dto.getBonus())
                .deduction(dto.getDeduction())
                .performanceRating(dto.getPerformanceRating() != null ? Double.valueOf(dto.getPerformanceRating()) : null)
                .isActive(dto.getActive())
                .password(dto.getPassword() != null && !dto.getPassword().isBlank() ? passwordEncoder.encode(dto.getPassword()) : null)
                .build();
    }



    // Create Employee
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Employee employee = mapToEntity(dto);
        return mapToDto(employeeRepository.save(employee));
    }

 // Update Employee
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));

        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getEmail() != null) employee.setEmail(dto.getEmail());
        if (dto.getPhone() != null) employee.setPhone(dto.getPhone());
        if (dto.getRole() != null) employee.setRole(USER_ROLE.valueOf(dto.getRole().toUpperCase()));
        if (dto.getDepartmentId() != null) employee.setDepartment(new Department(dto.getDepartmentId()));  // Assuming departmentId is enough
        if (dto.getBaseSalary() != null) employee.setBaseSalary(dto.getBaseSalary());
        if (dto.getBonus() != null) employee.setBonus(dto.getBonus());
        if (dto.getDeduction() != null) employee.setDeduction(dto.getDeduction());
        if (dto.getPerformanceRating() != null) employee.setPerformanceRating(Double.valueOf(dto.getPerformanceRating()));
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getActive() != null) employee.setIsActive(dto.getActive());

        // Update final salary after performance review update
        updateFinalSalary(employee);

        return mapToDto(employeeRepository.save(employee));
    }

    private void updateFinalSalary(Employee employee) {
        // Assuming final salary is baseSalary + bonus - deductions
        double finalSalary = employee.getBaseSalary() + employee.getBonus() - employee.getDeduction();
        employee.setFinalSalary(finalSalary);
        employeeRepository.save(employee); // Persist updated final salary
    }

    public EmployeeDto getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));
    }

    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Page<EmployeeDto> getAllEmployeesPaged(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::mapToDto);
    }


    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException("Employee with ID " + id + " not found");
        }
        employeeRepository.deleteById(id);
    }

    @Transactional
    public boolean toggleEmployeeStatus(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));

        boolean currentStatus = employee.getIsActive() != null && employee.getIsActive();
        employee.setIsActive(!currentStatus);
        employeeRepository.save(employee);
        return !currentStatus; // Return new status
    }
    
    
    public Page<EmployeeDto> getByRoleAndDepartment(String role, String department, Pageable pageable) {
        // Convert the role String to USER_ROLE enum
        USER_ROLE userRole = USER_ROLE.valueOf(role.toUpperCase());

        // Call the repository method with the USER_ROLE enum and department name
        return employeeRepository.findByRoleAndDepartment_Name(userRole, department, pageable)
                .map(this::mapToDto);
    }




}