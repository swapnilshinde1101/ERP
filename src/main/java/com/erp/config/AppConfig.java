package com.erp.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class AppConfig {

    @Value("${jwt.secret}")
    private String jwtSecret; // Ensure the JWT secret is defined in application.properties

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless authentication
            .authorizeHttpRequests(authz -> authz
                // Role-based access control for your ERP project
                .requestMatchers("/api/finance/**").hasRole("FINANCE") // Only Finance role can access finance-related APIs
                .requestMatchers("/api/hr/**").hasRole("HR") // Only HR role can access HR-related APIs
                .requestMatchers("/api/inventory/**").hasRole("INVENTORY") // Only Inventory role can access inventory-related APIs
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Only Admin can access admin-related APIs
                .requestMatchers("/api/**").authenticated() // All authenticated users can access these APIs
                .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                .anyRequest().permitAll()  // Public access for other endpoints (e.g., login, public info)
            )
            .addFilterBefore(new JwtTokenValidator(jwtSecret), BasicAuthenticationFilter.class) // JWT token validation
            .csrf(csrf -> csrf.disable())  // Disable CSRF since we're using JWT
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));  // Configure CORS for frontend access

        return http.build();
    }

    // CORS configuration allows frontend to make API requests to the backend
    private CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();
                cfg.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",  
                    "http://localhost:8080"
                ));
                cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                cfg.setAllowCredentials(true);  // Allow credentials like JWT token
                cfg.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
                cfg.setExposedHeaders(Arrays.asList("Authorization")); // Expose the JWT token in the response headers
                cfg.setMaxAge(3600L); // Cache pre-flight response for 1 hour
                return cfg;
            }
        };
    }

    // Password encoder to hash passwords securely
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
