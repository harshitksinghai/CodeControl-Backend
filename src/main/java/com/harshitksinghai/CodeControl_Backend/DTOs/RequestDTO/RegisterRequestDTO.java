package com.harshitksinghai.CodeControl_Backend.DTOs.RequestDTO;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String email;
    private String password;
}
