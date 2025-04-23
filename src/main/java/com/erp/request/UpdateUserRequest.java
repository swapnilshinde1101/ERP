package com.erp.request;

import com.erp.entity.USER_ROLE;

public class UpdateUserRequest {

	private String fullName;
    private String email;
    private USER_ROLE role;
    
    
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public USER_ROLE getRole() {
		return role;
	}
	public void setRole(USER_ROLE role) {
		this.role = role;
	}


}
