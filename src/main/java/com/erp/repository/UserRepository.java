package com.erp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.erp.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByEmail(String email); // Improved the parameter name to match "email"
//    Optional<User> findByEmail(String email);

}
