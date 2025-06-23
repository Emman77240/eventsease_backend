package com.eventease.service;

import com.eventease.domain.entity.User;
import com.eventease.dto.UserRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    User save(User user);
    User createUser(UserRequest request);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    void delete(Long id);
    User update(User user);
} 