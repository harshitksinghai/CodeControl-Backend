package com.harshitksinghai.CodeControl_Backend.AuthService.Repositories;

import com.harshitksinghai.CodeControl_Backend.AuthService.Models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}
