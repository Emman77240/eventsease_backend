package com.eventease.controller;

import com.eventease.dto.*;
import com.eventease.common.Constant;
import com.eventease.domain.entity.User;
import com.eventease.service.UserService;
import com.eventease.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRoles().stream()
                        .findFirst()
                        .map(Enum::name)
                        .orElse(null)) // Default to USER if no roles
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRequest request) {
        log.debug("Received registration request for email: {}", request.getEmail());

        // Check if user already exists
        if (userService.existsByEmail(request.getEmail())) {
            log.debug("User already exists with email: {}", request.getEmail());
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .responseCode(Constant.SUCCESS)
                            .responseMessage(Constant.EMAIL_ALREADY_REGISTERED)
                            .build());
        }

        try {
            // Create new user and generate token
            User user = userService.createUser(request);
            String token = jwtService.generateToken(user);
            UserDTO userDTO = convertToDTO(user);

            log.debug("Successfully registered user with email: {}", request.getEmail());
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .user(userDTO)
                    .responseCode(Constant.SUCCESS)
                    .responseMessage(Constant.USER_CREATED_SUCCESSFULLY)

                    .build());
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .responseCode(Constant.FAILURE)
                            .responseMessage("Error registering user: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            UserDTO userDTO = convertToDTO(user);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .user(userDTO)
                    .responseCode(Constant.SUCCESS)
                    .responseMessage(Constant.LOGIN_SUCCESSFUL)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .responseCode(Constant.FAILURE)
                            .responseMessage(Constant.INVALID_CREDENTIALS)
                            .build());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String userEmail = jwtService.extractUsername(token);
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtService.isTokenValid(token, user)) {
                return ResponseEntity.ok(AuthResponse.builder()
                        .responseCode(Constant.SUCCESS)
                        .responseMessage(Constant.SUCCESS_MESSAGE)
                        .user(convertToDTO(user))
                        .build());
            }
        } catch (Exception e) {
            // Do nothing, will return invalid token response
        }

        return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                        .responseCode(Constant.FAILURE)
                        .responseMessage(Constant.INVALID_TOKEN)
                        .build());
    }

    @PostMapping("/social-login")
    public ResponseEntity<AuthResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        log.info("Received social login request for email: {}", request.getEmail());
        Optional<User> existingUser = userService.findByEmail(request.getEmail());

        User user = null;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setIdentifier(User.LoginIdentifier.SOCIAL);
            userService.update(user);
        } else {
            user = User.builder()
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .identifier(User.LoginIdentifier.SOCIAL)
                    .roles(Collections.singleton(User.Role.User)) // Default to USER role
                    .build();
            userService.save(user);
        }

        String token = jwtService.generateToken(user);
        UserDTO userDTO = convertToDTO(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .responseCode(Constant.SUCCESS)
                .responseMessage(Constant.SOCIAL_LOGIN_SUCCESS)
                .build());
    }
} 