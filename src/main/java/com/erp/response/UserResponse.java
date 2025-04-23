package com.erp.response;

import com.erp.entity.USER_ROLE;
import com.erp.entity.USER_STATUS;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private USER_ROLE role;
    private USER_STATUS status;
    private String department;

    public UserResponse(Long id, String fullName, String email, USER_ROLE role, USER_STATUS status, String department) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.department = department;
    }

    // Getters and Setters (you can also use Lombok @Data if preferred)
    // OR generate using IDE shortcut
}
