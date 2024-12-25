package com.harshitksinghai.CodeControl_Backend.Config;

import com.harshitksinghai.CodeControl_Backend.Config.Jwt.JwtAuthenticationFilter;
import com.harshitksinghai.CodeControl_Backend.Services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);


    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthService authService;

    // Constructor injection
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Lazy AuthService authService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Public endpoints
                        .anyRequest().authenticated() // Protect other endpoints
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            try {
                                LOG.info("OAuth2 Login Success");
                                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                                String registrationId = ((OAuth2AuthenticationToken) authentication)
                                        .getAuthorizedClientRegistrationId();

                                boolean success = false;
                                if ("google".equals(registrationId)) {
                                    success = authService.handleGoogleLogin(oauth2User, response);
                                } else if ("github".equals(registrationId)) {
                                    success = authService.handleGithubLogin(oauth2User, response);
                                }

                                if (!success) {
                                    response.sendRedirect("http://localhost:5173/login?error=true");
                                    return;
                                }

                                // Set the authentication in SecurityContext for checkOnBoard
                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                boolean checkOnBoardStatus = authService.checkOnBoard(request);
                                if (checkOnBoardStatus) {
                                    response.sendRedirect("http://localhost:5173/home");
                                } else {
                                    response.sendRedirect("http://localhost:5173/onboard");
                                }

                            } catch (Exception e) {
                                LOG.error("Error in OAuth2 success handler: ", e);
                                response.sendRedirect("http://localhost:5173/login?error=true");
                            }
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
