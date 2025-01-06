package com.harshitksinghai.CodeControl_Backend.NotesService.Services;

import com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO.NotesDownloadResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface NotesService {
    ResponseEntity<CommonResponseDTO> uploadNotesFile(String videoId, MultipartFile notesFile);

    boolean deleteNotesFile(String notesId);

    NotesDownloadResponseDTO downloadNotes(String notesId);
}
