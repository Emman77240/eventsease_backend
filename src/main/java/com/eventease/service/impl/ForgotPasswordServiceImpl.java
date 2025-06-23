package com.eventease.service.impl;

import com.eventease.common.Constant;
import com.eventease.common.EmailService;
import com.eventease.domain.entity.User;
import com.eventease.dto.AuthResponse;
import com.eventease.dto.EventResponse;
import com.eventease.dto.ForgotPasswordRecoverRequest;
import com.eventease.repository.UserRepository;
import com.eventease.service.ForgotPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

import static org.springframework.beans.MethodInvocationException.ERROR_CODE;

@Slf4j
@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EmailService emailService;
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public ResponseEntity<EventResponse> recoverPassword(ForgotPasswordRecoverRequest request) {


        if (!request.getEmail().matches(EMAIL_REGEX)) {

            return ResponseEntity.badRequest()
                    .body(EventResponse.builder()
                            .responseCode(Constant.ERROR)
                            .responseMessage(Constant.INVALID_EMAIL_FORMAT)
                            .build());
        }

        Optional<User> userEntity = userRepository.findByEmail(request.getEmail());

        if (userEntity.isPresent()) {
            User entity = userEntity.get();
            String newPassword = generateRandomPassword(6);

            try {
                emailService.sendSimpleEmail(
                        request.getEmail(),
                        "Reset Password",
                        "Your New Password After Reset Password is: " + newPassword
                );
            } catch (Exception e) {
                log.error("Error registering user: {}", e.getMessage(), e);
                return ResponseEntity.badRequest()
                        .body(EventResponse.builder()
                                .responseCode(Constant.FAILURE)
                                .responseMessage("Error registering user: " + e.getMessage())
                                .build());

            }

            entity.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(entity);
            log.info("Password reset successful for user: {}", request.getEmail());
            return ResponseEntity.ok(EventResponse.builder()
                    .responseCode(Constant.SUCCESS)
                    .responseMessage(Constant.PASSWORD_RECOVERED_SUCCESS)
                    .build());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(EventResponse.builder()
                            .responseCode(Constant.ERROR)
                            .responseMessage(Constant.USER_NOT_FOUND)
                            .build());
        }
    }

    public String generateRandomPassword(int length) {
        // Define character sets for the password
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialCharacters = "!@#$%^&*()-_+=<>?";
        String allCharacters = upperCaseLetters + lowerCaseLetters + digits + specialCharacters;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure the password includes at least one character from each category
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        // Fill the rest of the password length with random characters from all categories
        for (int i = 4; i < length; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Shuffle the characters to avoid predictable patterns
        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        SecureRandom random = new SecureRandom();
        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Swap characters
            char temp = characters[i];
            characters[i] = characters[index];
            characters[index] = temp;
        }
        return new String(characters);
    }
}

