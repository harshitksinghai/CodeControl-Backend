package com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO;

import lombok.Data;

@Data
public class CommonResponseDTO {
    private boolean status;
    private String message;
    private String util;
}
