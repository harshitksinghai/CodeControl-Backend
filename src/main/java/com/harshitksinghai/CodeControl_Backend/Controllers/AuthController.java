package com.harshitksinghai.CodeControl_Backend.Controllers;

import com.harshitksinghai.CodeControl_Backend.Config.Jwt.JwtUtils;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.*;
import com.harshitksinghai.CodeControl_Backend.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.Services.AuthService;
import com.harshitksinghai.CodeControl_Backend.Services.Impl.EmailServiceImpl;
import com.harshitksinghai.CodeControl_Backend.Services.RefreshTokenService;
import com.harshitksinghai.CodeControl_Backend.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

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

    @Autowired
    EmailServiceImpl emailService;

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

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOTPEmail(@RequestParam String email){
        return authService.sendOTPEmail(email);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CommonResponseDTO> verifyOTP(@RequestBody VerifyOTPRequestDTO verifyOTPRequestDTO){
        return authService.verifyOTP(verifyOTPRequestDTO);
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

    @PostMapping("/change-password-and-login")
    public ResponseEntity<CommonResponseDTO> changePasswordAndLogin(@RequestBody ChangePasswordAndLoginRequestDTO changePasswordAndLoginRequestDTO, HttpServletResponse response){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();

        boolean res = authService.changePasswordAndLogin(changePasswordAndLoginRequestDTO, response);
        commonResponseDTO.setStatus(res);
        if(res){
            commonResponseDTO.setMessage("Login successful!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("Login failed!");
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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        boolean success = authService.logout(request, response);
        if (success) {
            return ResponseEntity.ok("Logged out successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed.");
        }
    }

    @GetMapping("/clear-expired-otps-links")
    public ResponseEntity<String> clearExpiredOTPLink(){
        return authService.clearExpiredOTPsLinks();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshTokenAccess(request, response);
    }

}
