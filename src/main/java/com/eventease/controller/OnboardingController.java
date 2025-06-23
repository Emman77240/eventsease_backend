package com.eventease.controller;

import com.eventease.domain.entity.User;
import com.eventease.dto.UserRequest;
import com.eventease.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class OnboardingController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserRequest request) {
        Optional<User> existingUser = userService.findByEmail(request.getEmail());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getIdentifier() == User.LoginIdentifier.SOCIAL) {
                return ResponseEntity.badRequest()
                    .body("You had logged in with Google account. kindly logged in via Google");
            }
            return ResponseEntity.badRequest().body("Already have an account");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .identifier(User.LoginIdentifier.LOCAL)
                .build();

        userService.save(newUser);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.badRequest().body("Email or password is wrong");
        }

        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/sociallogin")
    public ResponseEntity<String> socialLogin(@Valid @RequestBody UserRequest request) {
        Optional<User> existingUser = userService.findByEmail(request.getEmail());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setIdentifier(User.LoginIdentifier.SOCIAL);
            userService.update(user);
        } else {
            User newUser = User.builder()
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .identifier(User.LoginIdentifier.SOCIAL)
                    .build();
            userService.save(newUser);
        }

        return ResponseEntity.ok("Social login successful");
    }
} 