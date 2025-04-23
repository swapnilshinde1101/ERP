package com.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.erp.config.JwtProvider;
import com.erp.entity.USER_ROLE;
import com.erp.entity.User;
import com.erp.repository.UserRepository;
import com.erp.request.LoginRequest;
import com.erp.response.AuthResponse;
import com.erp.service.CustomerUserDetailsService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private CustomerUserDetailsService customerUserDetailsService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody User user) {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return new ResponseEntity<AuthResponse>(new AuthResponse("Email already in use"), HttpStatus.BAD_REQUEST);
        }

        // Ensure the role is not null
        if (user.getRole() == null) {
            return new ResponseEntity<AuthResponse>(new AuthResponse("Role cannot be null"), HttpStatus.BAD_REQUEST);
        }

        // If role is prefixed with "ROLE_", strip it (if any)
        if (user.getRole().name().startsWith("ROLE_")) {
            user.setRole(USER_ROLE.valueOf(user.getRole().name().replace("ROLE_", "")));
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Generate JWT for the saved user
        String jwt = jwtProvider.generateToken(
            new UsernamePasswordAuthenticationToken(savedUser.getEmail(), savedUser.getPassword())
        );

        // Return response with the generated JWT
        return new ResponseEntity<AuthResponse>(
            new AuthResponse(jwt, "Registration success", savedUser.getRole()),
            HttpStatus.CREATED
        );
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // Load user details by email
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(email);

        // Validate password and user
        if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Authenticate user and generate JWT token
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String jwt = jwtProvider.generateToken(authentication);

        // Get the user's role from the authorities
        String role = authentication.getAuthorities().stream().findFirst().get().getAuthority();

        // Remove the "ROLE_" prefix to get the actual enum value
        String roleWithoutPrefix = role.replace("ROLE_", "");

        // Return response with JWT and the user's role
        return new ResponseEntity<>(
            new AuthResponse(jwt, "Login success", USER_ROLE.valueOf(roleWithoutPrefix)),
            HttpStatus.OK
        );
    }

}
