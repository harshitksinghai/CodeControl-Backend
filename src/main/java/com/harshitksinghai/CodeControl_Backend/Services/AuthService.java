package com.harshitksinghai.CodeControl_Backend.Services;

import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.LoginRequestDTO;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.OnBoardRequestDTO;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.RegisterRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    boolean login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);

    boolean register(RegisterRequestDTO registerUserRequestDTO, HttpServletResponse response);

    boolean verifyEmail(String email);

    boolean checkOnBoard(HttpServletRequest request);

    boolean onBoard(OnBoardRequestDTO onBoardRequestDTO, HttpServletRequest request);

    ResponseEntity<String> refreshTokenAccess(HttpServletRequest request, HttpServletResponse response);
}
