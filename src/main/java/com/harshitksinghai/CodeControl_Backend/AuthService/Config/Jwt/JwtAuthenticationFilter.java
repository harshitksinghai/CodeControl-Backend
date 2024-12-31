package com.harshitksinghai.CodeControl_Backend.AuthService.Config.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Debug log all cookies
            if (request.getCookies() != null) {
                logger.debug("Cookies present in request:");
                for (Cookie cookie : request.getCookies()) {
                    logger.debug("Cookie name: " + cookie.getName() + ", Path: " + cookie.getPath());
                }
            } else {
                logger.debug("No cookies present in request");
            }

            // Retrieve JWT from HTTP-only cookies
            Optional<Cookie> jwtCookie = getJwtCookie(request, "codecontrol-jwt");

            if (jwtCookie.isPresent()) {
                logger.debug("JWT cookie found");
                String jwtToken = jwtCookie.get().getValue();

                if (jwtUtils.validateToken(jwtToken)) {
                    String username = jwtUtils.getUsernameFromToken(jwtToken);
                    logger.debug("Valid JWT token found for user: " + username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("User authorities: " + userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    logger.debug("Invalid JWT token");
                }
            } else {
                logger.debug("No JWT cookie found");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: ", e);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<Cookie> getJwtCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }
}
