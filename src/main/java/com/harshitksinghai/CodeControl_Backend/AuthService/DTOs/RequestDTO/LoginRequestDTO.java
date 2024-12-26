package com.harshitksinghai.CodeControl_Backend.AuthService.DTOs.RequestDTO;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}
