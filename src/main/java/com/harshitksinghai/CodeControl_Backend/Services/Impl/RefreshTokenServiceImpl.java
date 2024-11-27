package com.harshitksinghai.CodeControl_Backend.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.Enums.Role;
import com.harshitksinghai.CodeControl_Backend.Models.RefreshToken;
import com.harshitksinghai.CodeControl_Backend.Repositories.RefreshTokenRepository;
import com.harshitksinghai.CodeControl_Backend.Services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final Logger LOG = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Value("${CodeControl-Backend.app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    @Value("${CodeControl-Backend.app.refreshTokenSlidingWindowPeriod}")
    private int refreshTokenSlidingWindowPeriod;

    @Override
    public RefreshToken createRefreshToken(String emailId)  {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (refreshTokenRepository.findByToken(token).isPresent());

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(token)
                .expiryDate(Instant.now().plusMillis(refreshTokenSlidingWindowPeriod))
                .emailId(emailId)
                .build();


        RefreshToken refreshToken = refreshTokenRepository.save(newRefreshToken);
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public String deleteByToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).get();
        refreshTokenRepository.delete(refreshToken);
        return "Refresh token deleted successfully!";
    }
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        Instant now = Instant.now();

        if (token.getExpiryDate().compareTo(now) < 0) {
            LOG.info("Refresh token expired!");
            refreshTokenRepository.delete(token);
            return null;
        }


        if (token.getExpiryDate().compareTo(now.plusMillis(refreshTokenSlidingWindowPeriod)) < 0) {
            LOG.info("Incrementing the expiration time of the refresh token");
            token.setExpiryDate(now.plusMillis(refreshTokenSlidingWindowPeriod));
            token.setToken(UUID.randomUUID().toString());
            return refreshTokenRepository.save(token);
        }

        return token;
    }

    @Override
    public int getJwtRefreshExpirationMs(String token) {
        // Fetch the refresh token entity using the provided token
        Optional<RefreshToken> refreshTokenOptional = findByToken(token);

        if (refreshTokenOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        RefreshToken refreshToken = refreshTokenOptional.get();

        // Calculate the remaining time in milliseconds
        Instant now = Instant.now();
        Instant expiryDate = refreshToken.getExpiryDate();

        if (expiryDate.isBefore(now)) {
            throw new IllegalStateException("Refresh token has already expired.");
        }

        return (int) (expiryDate.toEpochMilli() - now.toEpochMilli());
    }

    @Override
    public void deleteById(Long id) {
        refreshTokenRepository.deleteById(id);
    }


}
