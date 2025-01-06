package com.harshitksinghai.CodeControl_Backend.VideoService.Services;

import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.ResponseDTO.CommonResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface VideoService {
    ResponseEntity<CommonResponseDTO> uploadVideo(String title, MultipartFile videoFile);

    void processVideo(String videoId) throws IOException, InterruptedException;

    ResponseEntity<Resource> streamVideoHLSAdaptive(String videoId);

    ResponseEntity<Resource> streamVideoHLSSpecificQuality(String videoId, String quality, String segment);

    boolean deleteVideo(String videoId);

    boolean deleteVideoMetaData(String videoId);

    boolean deleteVideoFile(String videoId);

    ResponseEntity<CommonResponseDTO> updateVideo(String videoId, String title, MultipartFile videoFile);
}

