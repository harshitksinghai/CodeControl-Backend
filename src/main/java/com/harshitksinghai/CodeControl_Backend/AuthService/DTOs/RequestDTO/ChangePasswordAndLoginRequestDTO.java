package com.harshitksinghai.CodeControl_Backend.AuthService.DTOs.RequestDTO;

import lombok.Data;

@Data
public class ChangePasswordAndLoginRequestDTO {
    private String email;
    private String password;
}
