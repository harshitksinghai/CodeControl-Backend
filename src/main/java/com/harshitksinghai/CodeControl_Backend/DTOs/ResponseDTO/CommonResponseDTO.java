package com.harshitksinghai.CodeControl_Backend.DTOs.ResponseDTO;

import lombok.Data;

@Data
public class CommonResponseDTO {
    private boolean status;
    private String message;
    private String util;
}
