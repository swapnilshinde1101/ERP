package com.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.erp.entity.Department;
import com.erp.entity.USER_ROLE;
import com.erp.entity.USER_STATUS;
import com.erp.entity.User;
import com.erp.repository.DepartmentRepository;
import com.erp.repository.UserRepository;
import com.erp.request.CreateUserRequest;
import com.erp.request.ResetPasswordRequest;
import com.erp.request.UpdateUserRequest;
import com.erp.response.MessageResponse;
import com.erp.response.UserResponse;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	@Autowired
	private DepartmentRepository departmentRepository;

	
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ 1. Create User
    @PostMapping("/create-user-management")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email already exists!"));
        }

        // Fetch department using ID
        Department department = departmentRepository.findByNameIgnoreCase(request.getDepartmentName())
        	    .orElseThrow(() -> new RuntimeException("Department not found with name: " + request.getDepartmentName()));


        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(USER_STATUS.ACTIVE);
        user.setDepartment(department); 

        User saved = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "User created successfully!",
                       "userId", saved.getId(),
                       "email", saved.getEmail(),
                       "department", department.getName())
        );
    }


    // ✅ 2. Update User
    @PutMapping("/update-user/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found"));
        }

        User user = optionalUser.get();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    }

    // ✅ 3. Reset Password
    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<?> resetPassword(@PathVariable Long userId, @RequestBody ResetPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found"));
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
    }

    @PutMapping("/change-status/{userId}")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        }

        User user = optionalUser.get();
        USER_STATUS currentStatus = user.getStatus();

        // Toggle logic
        USER_STATUS newStatus = currentStatus == USER_STATUS.ACTIVE
                ? USER_STATUS.DISABLED
                : USER_STATUS.ACTIVE;

        user.setStatus(newStatus);
        userRepository.save(user);

        return ResponseEntity.ok(
                new MessageResponse("User status changed from " + currentStatus + " to " + newStatus)
        );
    }


    // ✅ 5. Delete User
    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found"));
        }

        userRepository.deleteById(userId);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    // ✅ 6. View All Users (with optional role/status filter)
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) USER_ROLE role,
            @RequestParam(required = false) USER_STATUS status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAll(pageable);

        List<User> filtered = usersPage.getContent();
        if (role != null) {
            filtered = filtered.stream()
                    .filter(u -> u.getRole() == role)
                    .collect(Collectors.toList());
        }
        if (status != null) {
            filtered = filtered.stream()
                    .filter(u -> u.getStatus() == status)
                    .collect(Collectors.toList());
        }

        // Convert to DTOs
        List<UserResponse> response = filtered.stream().map(user ->
                new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus(),
                        user.getDepartment() != null ? user.getDepartment().getName() : null
                )
        ).collect(Collectors.toList());

        // Add metadata
        Map<String, Object> result = new HashMap<>();
        result.put("users", response);
        result.put("currentPage", usersPage.getNumber());
        result.put("totalItems", usersPage.getTotalElements());
        result.put("totalPages", usersPage.getTotalPages());

        return ResponseEntity.ok(result); // ✅ Only this return
    }

}
