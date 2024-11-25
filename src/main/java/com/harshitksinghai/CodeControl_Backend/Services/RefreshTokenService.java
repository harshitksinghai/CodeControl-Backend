package com.harshitksinghai.CodeControl_Backend.Services;

import com.harshitksinghai.CodeControl_Backend.Enums.Role;
import com.harshitksinghai.CodeControl_Backend.Models.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String emailId);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    int getJwtRefreshExpirationMs(String token);
}
