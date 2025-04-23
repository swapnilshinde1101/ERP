package com.erp.response;

import com.erp.entity.USER_ROLE;

import lombok.Getter;
import lombok.Setter;



public class AuthResponse {

    private String jwt;
    private String message;
    private USER_ROLE role;

    // Constructor with message only (for errors)
    public AuthResponse(String message) {
        this.message = message;
    }

    // Constructor with all fields (for success cases)
    public AuthResponse(String jwt, String message, USER_ROLE role) {
        this.jwt = jwt;
        this.message = message;
        this.role = role;
    }

    // Getters and Setters
    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public USER_ROLE getRole() {
        return role;
    }

    public void setRole(USER_ROLE role) {
        this.role = role;
    }
}

