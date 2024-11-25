package com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}
