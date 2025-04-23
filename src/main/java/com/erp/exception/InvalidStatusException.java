package com.erp.exception;

public class InvalidStatusException extends RuntimeException {

    // Default constructor
    public InvalidStatusException() {
        super("Invalid status encountered.");
    }

    // Constructor that accepts a custom message
    public InvalidStatusException(String message) {
        super(message);
    }

    // Constructor that accepts a custom message and a cause
    public InvalidStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public InvalidStatusException(Throwable cause) {
        super(cause);
    }
}
