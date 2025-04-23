package com.erp.service;

import com.erp.dto.DepartmentDTO;
import com.erp.entity.Department;
import com.erp.exception.DepartmentNotFoundException;
import com.erp.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setName(departmentDTO.getName());

        logger.info("Creating department: {}", department.getName());
        Department saved = departmentRepository.save(department);

        return mapToDTO(saved);
    }

    public List<DepartmentDTO> getAllDepartments() {
        logger.info("Fetching all departments");
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Department not found with ID: {}", id);
                    return new DepartmentNotFoundException(id);
                });

        return mapToDTO(dept);
    }

    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Department not found with ID: {}", id);
                    return new DepartmentNotFoundException(id);
                });

        boolean updated = false;

        if (departmentDTO.getName() != null && !departmentDTO.getName().trim().isEmpty()) {
            if (!dept.getName().equals(departmentDTO.getName())) {
                dept.setName(departmentDTO.getName());
                updated = true;
            }
        }

        if (updated) {
            Department updatedDept = departmentRepository.save(dept);
            logger.info("Updated department with ID: {}", id);
            return mapToDTO(updatedDept);
        } else {
            logger.info("No changes detected for department ID: {}", id);
            return mapToDTO(dept);
        }
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            logger.error("Department not found with ID: {}", id);
            throw new DepartmentNotFoundException(id);
        }

        departmentRepository.deleteById(id);
        logger.info("Deleted department with ID: {}", id);
    }

    // Mapper method
    private DepartmentDTO mapToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        return dto;
    }
}
