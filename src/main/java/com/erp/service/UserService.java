package com.erp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.config.JwtProvider;
import com.erp.entity.User;
import com.erp.repository.UserRepository;
import com.erp.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class  UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    
    public User findUserByJwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailfromjwtToken(jwt); // Extract email from JWT

        if (email == null || email.isEmpty()) {
            throw new Exception("Invalid or expired JWT token: Email could not be extracted");
        }

        return findUserByEmail(email); // Fetch the user using the email
    }

    
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email); // Query the repository for the user
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user;
    }
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void signup(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt the password
        userRepository.save(user);
    }

}
