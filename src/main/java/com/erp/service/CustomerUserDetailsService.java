package com.erp.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.erp.entity.User;
import com.erp.entity.Employee;
import com.erp.repository.EmployeeRepository;
import com.erp.repository.UserRepository;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Try to load from User (Admin, HR, Finance, Inventory)
        Optional<User> userOpt = Optional.ofNullable(userRepository.findByEmail(email));
        if (userOpt.isPresent() && userOpt.get().isActive()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        }

        // 2. Try to load from Employee
        Optional<Employee> empOpt = employeeRepository.findByEmail(email);
        if (empOpt.isPresent() && Boolean.TRUE.equals(empOpt.get().getIsActive())) {
            Employee emp = empOpt.get();
            return new org.springframework.security.core.userdetails.User(
                emp.getEmail(),
                emp.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
            );
        }

        // 3. If neither found or inactive, throw exception
        throw new UsernameNotFoundException("Invalid username or password");
    }
}
