package com.harshitksinghai.CodeControl_Backend.VideoService.Controllers;

import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.ResponseDTO.VideoResponseDTO;
import com.harshitksinghai.CodeControl_Backend.VideoService.Services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    VideoService videoService;

    @GetMapping("/")
    @PreAuthorize("hasAuthority('RESOURCE_READ')")
    public VideoResponseDTO getVideoByVideoId(String videoId){
        return null;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('RESOURCE_CREATE')")
    public ResponseEntity<CommonResponseDTO> uploadVideo(@RequestParam("title") String title, @RequestParam("file") MultipartFile videoFile){
        return videoService.uploadVideo(title, videoFile);
    }

    @GetMapping("/stream/{videoId}")
    @PreAuthorize("hasAuthority('RESOURCE_READ')")
    public ResponseEntity<Resource> streamVideo(@PathVariable("videoId") String videoId){
        return videoService.streamVideo(videoId);
    }

    @GetMapping("/stream/range/{videoId}")
    @PreAuthorize("hasAuthority('RESOURCE_READ')")
    public ResponseEntity<Resource> streamVideoRange(@PathVariable("videoId") String videoId, @RequestHeader(value = "Range", required = false) String range){
        System.out.println("Range: " + range);
        return videoService.streamVideoRange(videoId, range);
    }

}
