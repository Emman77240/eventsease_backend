package com.eventease.controller;

import com.eventease.dto.EventResponse;
import com.eventease.dto.ForgotPasswordRecoverRequest;
import com.eventease.service.ForgotPasswordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    private static final Logger logger = LogManager.getLogger(ForgotPasswordController.class);


    private final ForgotPasswordService forgotPasswordService;
    @Autowired
    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/recover-forgotten-password")
    public ResponseEntity<EventResponse> recoverForgottonPassword(@RequestBody ForgotPasswordRecoverRequest request) {
        logger.info("Forgot password recovery request recieved :: {}" , request);
        ResponseEntity<EventResponse> response = forgotPasswordService.recoverPassword(request);
        return response;
    }
}
