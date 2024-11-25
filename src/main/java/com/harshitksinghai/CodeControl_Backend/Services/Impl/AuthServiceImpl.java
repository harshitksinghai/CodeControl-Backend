package com.harshitksinghai.CodeControl_Backend.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.Config.Jwt.CookieUtils;
import com.harshitksinghai.CodeControl_Backend.Config.Jwt.JwtUtils;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.LoginRequestDTO;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.OnBoardRequestDTO;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.RegisterRequestDTO;
import com.harshitksinghai.CodeControl_Backend.Enums.Role;
import com.harshitksinghai.CodeControl_Backend.Models.RefreshToken;
import com.harshitksinghai.CodeControl_Backend.Models.User;
import com.harshitksinghai.CodeControl_Backend.Repositories.UserRepository;
import com.harshitksinghai.CodeControl_Backend.Services.AuthService;
import com.harshitksinghai.CodeControl_Backend.Services.RefreshTokenService;
import com.harshitksinghai.CodeControl_Backend.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Value("${CodeControl-Backend.app.jwtCookieName}")
    private String jwtCookieName;

    @Value("${CodeControl-Backend.app.jwtRefreshCookieName}")
    private String jwtRefreshCookieName;

    @Value("${CodeControl-Backend.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Override
    public boolean verifyEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }

    @Override
    public boolean login(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
            );

            String email = loginRequestDTO.getEmail();
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            String jwtToken = jwtUtils.generateToken(email, role);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

            CookieUtils.addCookie(response, jwtCookieName, jwtToken, jwtExpirationMs);
            CookieUtils.addCookie(response, jwtRefreshCookieName, refreshToken.getToken(), refreshTokenService.getJwtRefreshExpirationMs(refreshToken.getToken()));

            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean register(RegisterRequestDTO registerRequestDTO, HttpServletResponse response) {
        String email = registerRequestDTO.getEmail();

        boolean checkEmail = verifyEmail(email);
        if(!checkEmail){
            User user = new User();
            user.setEmail(email);
            user.setRole(Role.USER);
            user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));


            String role = String.valueOf(Role.USER);
            String jwtToken = jwtUtils.generateToken(email, role);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

            CookieUtils.addCookie(response, jwtCookieName, jwtToken, jwtExpirationMs);
            CookieUtils.addCookie(response, jwtRefreshCookieName, refreshToken.getToken(), refreshTokenService.getJwtRefreshExpirationMs(refreshToken.getToken()));

            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean checkOnBoard(HttpServletRequest request) {
        // Extract email from JWT in HttpOnly cookie
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Extracted from the JWT

        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.get().getFirstName() != null && userOpt.get().getLastName() != null){
            return true;
        }
        return false;
    }

    @Override
    public boolean onBoard(OnBoardRequestDTO onBoardRequestDTO, HttpServletRequest request) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Optional<User> userOpt = userRepository.findByEmail(email);
            User user = userOpt.get();
            user.setFirstName(onBoardRequestDTO.getFirstName());
            user.setMiddleName(onBoardRequestDTO.getMiddleName());
            user.setLastName(onBoardRequestDTO.getLastName());
            userRepository.save(user);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public ResponseEntity<String> refreshTokenAccess(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No cookies found.");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if (jwtRefreshCookieName.equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token not found in cookies.");
        }

        Optional<RefreshToken> exists = refreshTokenService.findByToken(refreshToken);
        if (exists.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid refresh token.");
        }

        RefreshToken updatedToken = refreshTokenService.verifyExpiration(exists.get());
        if (updatedToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token expired and deleted.");
        }

        // Retrieve email and role from refresh token
        String emailId = updatedToken.getEmailId();
        String role = userService.getRoleByEmail(emailId);

        // Generate new access token
        String accessToken = jwtUtils.generateToken(emailId, role);

        // Delete old cookies before setting new ones
        //CookieUtils.deleteCookie(response, jwtCookieName);
        //CookieUtils.deleteCookie(response, jwtRefreshCookieName);

        // Add updated tokens as HTTP-only cookies
        CookieUtils.addCookie(response, jwtCookieName, accessToken, jwtExpirationMs);
        CookieUtils.addCookie(response, jwtRefreshCookieName, updatedToken.getToken(), refreshTokenService.getJwtRefreshExpirationMs(updatedToken.getToken()));

        return ResponseEntity.ok("Tokens refreshed successfully.");
    }


}

















