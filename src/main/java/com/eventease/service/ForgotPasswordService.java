package com.eventease.service;

import com.eventease.dto.EventRequest;
import com.eventease.dto.EventResponse;
import com.eventease.dto.ForgotPasswordRecoverRequest;
import org.springframework.http.ResponseEntity;

public interface ForgotPasswordService {
    ResponseEntity<EventResponse> recoverPassword(ForgotPasswordRecoverRequest request);
}
