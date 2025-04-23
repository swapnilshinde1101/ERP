package com.erp.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentDTO {
    
    private Long id;

    @NotBlank(message = "Department name is required")
    private String name;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
