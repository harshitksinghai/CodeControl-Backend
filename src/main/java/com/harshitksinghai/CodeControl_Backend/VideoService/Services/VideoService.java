package com.harshitksinghai.CodeControl_Backend.VideoService.Services;

import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.RequestDTO.VideoUploadRequestDTO;
import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.ResponseDTO.CommonResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public interface VideoService {
    ResponseEntity<CommonResponseDTO> uploadVideo(String title, MultipartFile videoFile);

    ResponseEntity<Resource> streamVideo(String videoId);

    ResponseEntity<Resource> streamVideoRange(String videoId, String range);
}

