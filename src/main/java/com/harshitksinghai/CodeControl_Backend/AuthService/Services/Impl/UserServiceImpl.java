package com.harshitksinghai.CodeControl_Backend.AuthService.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.AuthService.Repositories.UserRepository;
import com.harshitksinghai.CodeControl_Backend.AuthService.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Override
    public String getRoleByEmail(String emailId) {
        return userRepository.findByEmail(emailId)
                .map(user -> user.getRole().name()) // Convert Enum Role to String
                .orElseThrow(() -> new IllegalArgumentException("User with email " + emailId + " not found"));
    }
}
