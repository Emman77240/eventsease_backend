package com.eventease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Object data;
    private String responseCode;
    private String responseMessage;
    private String token;
    private UserDTO user;

} 