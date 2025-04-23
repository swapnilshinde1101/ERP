package com.erp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends RuntimeException {

    // Constructor with custom message
    public EmployeeNotFoundException(String message) {
        super(message);
    }

    // Default constructor for generic cases
    public EmployeeNotFoundException() {
        super("Employee not found");
    }
}
