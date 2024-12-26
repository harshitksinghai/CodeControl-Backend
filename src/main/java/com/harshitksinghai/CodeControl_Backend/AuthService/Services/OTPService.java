package com.harshitksinghai.CodeControl_Backend.AuthService.Services;

import org.springframework.transaction.annotation.Transactional;

public interface OTPService {

    String generateOTP();

    @Transactional
    void addOTPDetails(String email, String otp);

    @Transactional
    boolean verifyOTP(String email, String otp);

    @Transactional
    void clearExpiredOTPs();
}
