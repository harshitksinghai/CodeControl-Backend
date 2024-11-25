package com.harshitksinghai.CodeControl_Backend.Repositories;

import com.harshitksinghai.CodeControl_Backend.Models.RefreshToken;
import com.harshitksinghai.CodeControl_Backend.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}
