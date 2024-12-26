package com.harshitksinghai.CodeControl_Backend.AuthService.Repositories;

import com.harshitksinghai.CodeControl_Backend.Models.LinkVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LinkVerificationRepository extends JpaRepository<LinkVerification, Long> {
    Optional<LinkVerification> findByCode(String code);

    void deleteByCode(String code);

    void deleteByExpirationTimeBefore(LocalDateTime now);
}
