package com.harshitksinghai.CodeControl_Backend.AuthService.Repositories;

import com.harshitksinghai.CodeControl_Backend.AuthService.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
