package com.harshitksinghai.CodeControl_Backend.Services;

import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.*;
import com.harshitksinghai.CodeControl_Backend.DTOs.ResponseDTO.CommonResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    boolean login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);

    ResponseEntity<CommonResponseDTO> verifyOTP(VerifyOTPRequestDTO verifyOTPRequestDTO);

    boolean register(RegisterRequestDTO registerUserRequestDTO, HttpServletResponse response);

    ResponseEntity<CommonResponseDTO> verifyEmail(String email);

    @Transactional
    boolean changePasswordAndLogin(ChangePasswordAndLoginRequestDTO changePasswordAndLoginRequestDTO, HttpServletResponse response);

    boolean checkOnBoard(HttpServletRequest request);

    boolean onBoard(OnBoardRequestDTO onBoardRequestDTO, HttpServletRequest request);

    ResponseEntity<String> clearExpiredOTPsLinks();

    boolean logout(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<String> refreshTokenAccess(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<CommonResponseDTO> sendOTPEmail(String email);
}
