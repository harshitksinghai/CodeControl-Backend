package com.harshitksinghai.CodeControl_Backend.AuthService.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.AuthService.Models.OTPVerification;
import com.harshitksinghai.CodeControl_Backend.AuthService.Repositories.OTPVerificationRepository;
import com.harshitksinghai.CodeControl_Backend.AuthService.Services.OTPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
public class OTPServiceImpl implements OTPService {
    private final Logger LOG = LoggerFactory.getLogger(OTPServiceImpl.class);

    @Autowired
    OTPVerificationRepository otpVerificationRepository;
    @Override
    public String generateOTP() {
        LOG.info("inside generateOTP in OTPServiceImpl");
        // Define the length of the OTP
        final int OTP_LENGTH = 6; // 6 characters long OTP

        // OTP characters allowed
        final String OTP_CHARACTERS = "0123456789";

        // Random generator
        final Random random = new SecureRandom();

        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(OTP_CHARACTERS.charAt(random.nextInt(OTP_CHARACTERS.length())));
        }
        LOG.info("otp generated");
        return otp.toString();
    }

    @Override
    public void addOTPDetails(String email, String otp) {
        LOG.info("inside addOTPDetails in OTPServiceImpl");
        OTPVerification otpVerification = new OTPVerification();
        otpVerification.setOtp(otp);
        otpVerification.setEmail(email);
        otpVerification.setExpirationTime(LocalDateTime.now().plusMinutes(1).plusSeconds(30));
        LOG.info("otp details added to db");
        otpVerificationRepository.save(otpVerification);
    }

    @Override
    public boolean verifyOTP(String email, String otp) {
        LOG.info("inside verifyOTP in OTPServiceImpl");

        Optional<OTPVerification> otpVerification = otpVerificationRepository.findByOtp(otp);
        if(otpVerification.isEmpty()){
            LOG.info("otp does not exist in db");
            return false;
        }
        if(!Objects.equals(otpVerification.get().getEmail(), email)){
            LOG.info("Invalid otp!");
            return false;
        }
        if(otpVerification.get().getExpirationTime().isAfter(LocalDateTime.now())){
            LOG.info("otp verified successfully, otp still not expired");
            otpVerificationRepository.deleteByOtp(otp);

            return true;
        }
        LOG.info("otp expired");
        otpVerificationRepository.deleteByOtp(otp);

        return false;
    }

    @Override
    public void clearExpiredOTPs() {
        LOG.info("inside clearExpiredOTPs in OTPServiceImpl");
        LocalDateTime now = LocalDateTime.now();
        otpVerificationRepository.deleteByExpirationTimeBefore(now);
    }
}

