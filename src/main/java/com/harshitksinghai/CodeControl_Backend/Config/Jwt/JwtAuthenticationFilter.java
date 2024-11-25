package com.harshitksinghai.CodeControl_Backend.Config.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Retrieve JWT from HTTP-only cookies
            Optional<Cookie> jwtCookie = getJwtCookie(request, "codecontrol-jwt");

            if (jwtCookie.isPresent()) {
                String jwtToken = jwtCookie.get().getValue();

                if (jwtUtils.validateToken(jwtToken)) {
                    String username = jwtUtils.getUsernameFromToken(jwtToken);
                    //System.out.println("jwtUtils.getUsernameFromToken(jwtToken); returned username: " + username);
                    // Load user details for authentication
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    // Set the authenticated user in the security context
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}");
            e.printStackTrace();
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
