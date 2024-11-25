package com.harshitksinghai.CodeControl_Backend.Controllers;

import com.harshitksinghai.CodeControl_Backend.Config.Jwt.CookieUtils;
import com.harshitksinghai.CodeControl_Backend.Config.Jwt.JwtUtils;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.LoginRequestDTO;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.OnBoardRequestDTO;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.RegisterRequestDTO;
import com.harshitksinghai.CodeControl_Backend.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.Models.RefreshToken;
import com.harshitksinghai.CodeControl_Backend.Services.AuthService;
import com.harshitksinghai.CodeControl_Backend.Services.RefreshTokenService;
import com.harshitksinghai.CodeControl_Backend.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {



    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    UserService userService;

    @Autowired
    AuthService authService;

    @Value("${CodeControl-Backend.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${CodeControl-Backend.app.jwtRefreshCookieName}")
    private String jwtRefreshCookieName;

    @PostMapping("/verify-email")
    public boolean verifyEmail(@RequestParam String email){
        return authService.verifyEmail(email);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();

        boolean res = authService.login(loginRequestDTO, response);
        commonResponseDTO.setStatus(res);
        if(res){
            commonResponseDTO.setMessage("Login successful!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("Bad credentials!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO, HttpServletResponse response){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();

        boolean res = authService.register(registerRequestDTO, response);
        commonResponseDTO.setStatus(res);
        if(res){
            commonResponseDTO.setMessage("Registration successful!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("User already exists!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/check-onboard")
    public ResponseEntity<CommonResponseDTO> checkOnBoard(HttpServletRequest request){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();

        boolean res = authService.checkOnBoard(request);
        commonResponseDTO.setStatus(res);

        return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/onboard")
    public ResponseEntity<CommonResponseDTO> onBoard(@RequestBody OnBoardRequestDTO onBoardRequestDTO, HttpServletRequest request){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();

        boolean res = authService.onBoard(onBoardRequestDTO, request);
        commonResponseDTO.setStatus(res);
        if(res){
            commonResponseDTO.setMessage("On-boarding successful!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("Unable to on-board!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @





    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshTokenAccess(request, response);
    }


}
