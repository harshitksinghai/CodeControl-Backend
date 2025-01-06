package com.harshitksinghai.CodeControl_Backend.VideoService.Controllers;

import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.VideoService.Services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    // hide video from users - but that's frontend right
    // update video file
    // update video metadata
    // delete video file

    @Autowired
    VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('RESOURCE_CREATE')")
    public ResponseEntity<CommonResponseDTO> uploadVideo(@RequestParam("title") String title, @RequestParam("file") MultipartFile videoFile){
        return videoService.uploadVideo(title, videoFile);
    }

    @GetMapping("/stream/{videoId}/master.m3u8")
    @PreAuthorize("hasAuthority('RESOURCE_READ')")
    public ResponseEntity<Resource> getAdaptiveStream(@PathVariable("videoId") String videoId) {
        return videoService.streamVideoHLSAdaptive(videoId);
    }

    @GetMapping("/stream/{videoId}/{quality}/{segment:.*}")
    @PreAuthorize("hasAuthority('RESOURCE_READ')")
    public ResponseEntity<Resource> getSpecificQualityStream(
            @PathVariable("videoId") String videoId,
            @PathVariable("quality") String quality,
            @PathVariable("segment") String segment) {
        return videoService.streamVideoHLSSpecificQuality(videoId, quality, segment);
    }

    // video and metadata deletion - two separate steps, what if metadata deleted but video deletion failed? how to rollback? and Visa-Versa?
    @DeleteMapping("/delete/{videoId}")
    @PreAuthorize("hasAuthority('RESOURCE_DELETE')")
    public ResponseEntity<CommonResponseDTO> deleteVideo(@PathVariable("videoId") String videoId){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        boolean res = videoService.deleteVideo(videoId);
        commonResponseDTO.setStatus(res);
        if(res){
            commonResponseDTO.setMessage("Video deleted successfully!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("Unable to delete video!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/video-file/{videoId}")
    @PreAuthorize("hasAuthority('RESOURCE_DELETE')")
    public ResponseEntity<CommonResponseDTO> deleteVideoFile(@PathVariable("videoId") String videoId){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        boolean res = videoService.deleteVideoFile(videoId);
        commonResponseDTO.setStatus(res);
        if(res){
            commonResponseDTO.setMessage("Video deleted successfully!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("Unable to delete video!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{videoId}")
    @PreAuthorize("hasAuthority('RESOURCE_UPDATE')")
    public ResponseEntity<CommonResponseDTO> updateVideo(@PathVariable("videoId") String videoId,@RequestParam("title") String title,@RequestParam(value = "file", required = false) MultipartFile videoFile){
        return videoService.updateVideo(videoId, title, videoFile);
    }
}
