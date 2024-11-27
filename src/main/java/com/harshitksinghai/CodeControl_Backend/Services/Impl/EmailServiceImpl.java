package com.harshitksinghai.CodeControl_Backend.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.Services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final Logger LOG = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("$(spring.mail.username)")
    private String fromMail;
    @Override
    public void sendOTPLinkEmail(String email, String otp, String link) {
        LOG.info("inside sendOTPLinkEmail in EmailServiceImpl");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setSubject("Your OTP and Magic Link");
        simpleMailMessage.setText("Your OTP is: " + otp + "\nUse the following link to complete your signup: " + link);
        simpleMailMessage.setTo(email);

        mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendOTPEmail(String email, String otp) {
        LOG.info("inside sendOTPEmail in EmailServiceImpl");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setSubject("Your OTP for email verification");
        simpleMailMessage.setText("Your OTP is: " + otp);
        simpleMailMessage.setTo(email);

        mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendResetPasswordLink(String email, String link) {
        LOG.info("inside sendResetPasswordLink in EmailServiceImpl");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setSubject("Your Reset Password Link");
        simpleMailMessage.setText("Here is your reset password link:\n" + link);
        simpleMailMessage.setTo(email);

        mailSender.send(simpleMailMessage);
    }
}