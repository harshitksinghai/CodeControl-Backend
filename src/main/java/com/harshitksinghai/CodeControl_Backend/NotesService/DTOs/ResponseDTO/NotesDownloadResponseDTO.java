package com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class NotesDownloadResponseDTO {
    private Resource resource;
    private String contentType;
    private String fileName;
}
