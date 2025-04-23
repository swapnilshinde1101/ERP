package com.erp.exception;

import com.erp.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<MessageResponse> handleDepartmentNotFoundException(DepartmentNotFoundException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LeaveRequestException.class)
    public ResponseEntity<MessageResponse> handleLeaveRequestException(LeaveRequestException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException(Exception e) {
        return new ResponseEntity<>(new MessageResponse("Something went wrong: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
