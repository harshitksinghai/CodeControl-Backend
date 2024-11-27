package com.harshitksinghai.CodeControl_Backend.Services;

public interface EmailService {

    void sendOTPLinkEmail(String email, String otp, String link);

    void sendOTPEmail(String email, String otp);

    void sendResetPasswordLink(String email, String link);
}

