package com.harshitksinghai.CodeControl_Backend.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.Config.Jwt.CookieUtils;
import com.harshitksinghai.CodeControl_Backend.Config.Jwt.JwtUtils;
import com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO.*;
import com.harshitksinghai.CodeControl_Backend.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.Enums.Role;
import com.harshitksinghai.CodeControl_Backend.Models.RefreshToken;
import com.harshitksinghai.CodeControl_Backend.Models.User;
import com.harshitksinghai.CodeControl_Backend.Repositories.UserRepository;
import com.harshitksinghai.CodeControl_Backend.Services.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final UserService userService;

    // Constructor injection instead of @Autowired
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Autowired
    OTPService otpService;

    @Autowired
    LinkService linkService;

    @Autowired
    EmailService emailService;

    @Value("${CodeControl-Backend.app.jwtCookieName}")
    private String jwtCookieName;

    @Value("${CodeControl-Backend.app.jwtRefreshCookieName}")
    private String jwtRefreshCookieName;

    @Value("${CodeControl-Backend.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${site.url}")
    private String siteURL;

    @Override
    public ResponseEntity<CommonResponseDTO> verifyEmail(String email) {
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if(userOpt.isPresent()){
                commonResponseDTO.setStatus(true);
                if(userOpt.get().getPassword() != null){
                    commonResponseDTO.setUtil("true");
                }
                else{
                    commonResponseDTO.setUtil("false");
                }
                return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
            }
            else{
                commonResponseDTO.setStatus(false);
                return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);

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

            Optional<User> userOpt = userRepository.findByEmail(email);
            userOpt.get().setRefreshToken(refreshToken);
            userRepository.save(userOpt.get());

            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ResponseEntity<CommonResponseDTO> sendOTPEmail(String email) {
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        try{
            String otp = otpService.generateOTP();
            otpService.addOTPDetails(email, otp);

            emailService.sendOTPEmail(email, otp);
            commonResponseDTO.setStatus(true);
            commonResponseDTO.setMessage("OTP sent successfully!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace();
            commonResponseDTO.setStatus(false);
            commonResponseDTO.setMessage("Unable to send otp!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<CommonResponseDTO> verifyOTP(VerifyOTPRequestDTO verifyOTPRequestDTO) {
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();

        boolean isVerified = otpService.verifyOTP(verifyOTPRequestDTO.getEmail(), verifyOTPRequestDTO.getOtp());
        commonResponseDTO.setStatus(isVerified);
        if(isVerified){
            commonResponseDTO.setMessage("OTP is verified!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("Invalid OTP!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public boolean handleGoogleLogin(OAuth2User principal, HttpServletResponse response) {
        System.out.println("handleGoogleLogin called with user: " + principal.getAttributes());

        String googleId = principal.getAttribute("sub");
        String email = principal.getAttribute("email");
        String firstName = principal.getAttribute("given_name");
        String lastName = principal.getAttribute("family_name");

        String role = String.valueOf(Role.USER);
        String jwtToken = jwtUtils.generateToken(email, role);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

        // Check if user exists
        Optional<User> userOpt = userRepository.findByEmail(email);

        if(userOpt.isEmpty()){
            User newUser = new User();
            newUser.setGoogleId(googleId);
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setRole(Role.USER); // Default role
            newUser.setRefreshToken(refreshToken);
            userRepository.save(newUser);
        }
        else{
            User user = userOpt.get();

            // Link GitHub ID if not linked
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }

        CookieUtils.addCookie(response, jwtCookieName, jwtToken, jwtExpirationMs);
        CookieUtils.addCookie(response, jwtRefreshCookieName, refreshToken.getToken(), refreshTokenService.getJwtRefreshExpirationMs(refreshToken.getToken()));

        return true;
    }

    @Override
    public boolean handleGithubLogin(OAuth2User principal, HttpServletResponse response) {
        System.out.println("handleGithubLogin called with user: " + principal.getAttributes());

        String githubId = principal.getAttribute("id").toString();
        String email = principal.getAttribute("email");
        String username = principal.getAttribute("login");

        String role = String.valueOf(Role.USER);
        String jwtToken = jwtUtils.generateToken(email, role);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

        // Check if user exists
        Optional<User> userOpt = userRepository.findByEmail(email);

        if(userOpt.isEmpty()){
            User newUser = new User();
            newUser.setGithubId(githubId);
            newUser.setEmail(email);
            //newUser.setFirstName(username); // Use GitHub username
            newUser.setRole(Role.USER); // Default role
            newUser.setRefreshToken(refreshToken);
            userRepository.save(newUser);
        }
        else{
            User user = userOpt.get();

            // Link GitHub ID if not linked
            if (user.getGithubId() == null) {
                user.setGithubId(githubId);
            }
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }

        CookieUtils.addCookie(response, jwtCookieName, jwtToken, jwtExpirationMs);
        CookieUtils.addCookie(response, jwtRefreshCookieName, refreshToken.getToken(), refreshTokenService.getJwtRefreshExpirationMs(refreshToken.getToken()));

        return true;
    }

    @Override
    public boolean register(RegisterRequestDTO registerRequestDTO, HttpServletResponse response) {
        String email = registerRequestDTO.getEmail();

        try{
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
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changePasswordAndLogin(ChangePasswordAndLoginRequestDTO changePasswordAndLoginRequestDTO, HttpServletResponse response) {
        String email = changePasswordAndLoginRequestDTO.getEmail();

        try{
            Optional<User> userOpt = userRepository.findByEmail(email);
            User user = userOpt.get();
            String role = String.valueOf(user.getRole());
            user.setPassword(passwordEncoder.encode(changePasswordAndLoginRequestDTO.getPassword()));

            String jwtToken = jwtUtils.generateToken(email, role);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

            CookieUtils.addCookie(response, jwtCookieName, jwtToken, jwtExpirationMs);
            CookieUtils.addCookie(response, jwtRefreshCookieName, refreshToken.getToken(), refreshTokenService.getJwtRefreshExpirationMs(refreshToken.getToken()));

            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkOnBoard(HttpServletRequest request) {
        try {
            // Try to get authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email;

            if (authentication instanceof OAuth2AuthenticationToken) {
                // If it's OAuth2 authentication, get email from OAuth2User
                OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
                email = oauth2User.getAttribute("email");
            } else {
                // For JWT authentication
                email = authentication.getName();
            }

            if (email == null) {
                return false;
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            return userOpt.map(user ->
                    user.getFirstName() != null && user.getLastName() != null
            ).orElse(false);

        } catch (Exception e) {
            LOG.error("Error checking onboard status: ", e);
            return false;
        }
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
    public ResponseEntity<String> clearExpiredOTPs() {
        LOG.info("inside clearExpiredOTPsLinks in UserAuthServiceImpl");

        LOG.info("clearing expired otps");
        otpService.clearExpiredOTPs();
        LOG.info("expired otps cleared successfully");

        return new ResponseEntity<>("successfully cleared expired otps", HttpStatus.OK);
    }

    @Override
    public boolean logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Existing cookie cleanup
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (jwtCookieName.equals(cookie.getName()) ||
                            jwtRefreshCookieName.equals(cookie.getName())){

                        CookieUtils.deleteCookie(response, cookie.getName());
                    }
                }
            }

            // Get the email and authentication details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();

                // Handle database cleanup
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    RefreshToken refreshToken = user.getRefreshToken();

                    user.setRefreshToken(null);
                    userRepository.save(user);
                    if (refreshToken != null) {
                        refreshTokenService.deleteById(refreshToken.getId());
                    }
                }
            }

            // Clear security context
            SecurityContextHolder.clearContext();

            return true;
        } catch (Exception e) {
            LOG.error("Error during logout: ", e);
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

















