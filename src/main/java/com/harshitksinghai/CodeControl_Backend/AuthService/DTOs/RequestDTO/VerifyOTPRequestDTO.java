package com.harshitksinghai.CodeControl_Backend.AuthService.DTOs.RequestDTO;

import lombok.Data;

@Data
public class VerifyOTPRequestDTO {
    private String email;
    private String otp;
}