package com.erp.exception;

public class PayslipNotFoundException extends RuntimeException {
    public PayslipNotFoundException(String message) {
        super(message);
    }
}
