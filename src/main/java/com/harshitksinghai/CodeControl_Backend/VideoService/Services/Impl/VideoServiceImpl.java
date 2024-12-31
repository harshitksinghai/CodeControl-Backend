package com.harshitksinghai.CodeControl_Backend.VideoService.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.AuthService.Models.User;
import com.harshitksinghai.CodeControl_Backend.AuthService.Repositories.UserRepository;
import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.VideoService.Enums.VideoStatus;
import com.harshitksinghai.CodeControl_Backend.VideoService.Models.Video;
import com.harshitksinghai.CodeControl_Backend.VideoService.Repositories.VideoRepository;
import com.harshitksinghai.CodeControl_Backend.VideoService.Services.VideoService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {
    private final Logger LOG = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    VideoRepository videoRepository;

    @Value("${files.upload.video}")
    String videoUploadDIR;

    @PostConstruct
    public void init(){
        File file = new File(videoUploadDIR);
        if(!file.exists()){
            file.mkdir();
            System.out.println("Folder created!");
        }
        else{
            System.out.println("Folder already created!");
        }
    }

    @Override
    public ResponseEntity<CommonResponseDTO> uploadVideo(String videoMetaData, MultipartFile videoFile) {
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        try{
            String videoFileName = videoFile.getOriginalFilename();
            String contentType = videoFile.getContentType();
            InputStream inputStream = videoFile.getInputStream();

            String cleanVideoFileName = StringUtils.cleanPath(videoFileName);
            String cleanVideoUploadDIR = StringUtils.cleanPath(videoUploadDIR);
            Path path = Paths.get(cleanVideoUploadDIR, cleanVideoFileName);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            Video video = new Video();
            video.setVideoId(UUID.randomUUID().toString());
            video.setStatus(VideoStatus.PENDING);
            video.setTitle(videoMetaData);
            video.setContentType(contentType);
            video.setUploadedBy(userOpt.get());
            video.setFilePath(path.toString());
            videoRepository.save(video);

            commonResponseDTO.setStatus(true);
            commonResponseDTO.setMessage("Video uploaded successfully!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();

            commonResponseDTO.setStatus(false);
            commonResponseDTO.setMessage("Video upload failed!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Resource> streamVideo(String videoId) {
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found for provided videoId: " + videoId));

        String filePath = video.getFilePath();
        String contentType = video.getContentType();
        if(contentType == null){
            contentType = "application/octet-stream";
        }

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> streamVideoRange(String videoId, String range) {
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found for provided videoId: " + videoId));

        String filePath = video.getFilePath();
        String contentType = video.getContentType();
        if(contentType == null){
            contentType = "application/octet-stream";
        }

        Resource resource = new FileSystemResource(filePath);
        if(range == null){
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        Path path = Paths.get(filePath);
        long fileLength = path.toFile().length();

        String[] ranges = range.replace("bytes=", "").split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd;
        if(ranges.length > 1 && !ranges[1].isEmpty()){
            rangeEnd = Long.parseLong(ranges[1]);
        }
        else{
            rangeEnd = fileLength-1;
        }
        if(rangeEnd>fileLength-1){
            rangeEnd = fileLength-1;
        }

        LOG.info("Range Start: " + rangeStart);
        LOG.info("Range End: " + rangeEnd);

        InputStream inputStream;

        try{
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

        } catch(IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        long contentLength = rangeEnd - rangeStart + 1;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        httpHeaders.add("Accept-Ranges", "bytes");
        httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("Expires", "0");
        httpHeaders.add("X-Content-Type-Options", "nosniff");
        httpHeaders.setContentLength(contentLength);

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .headers(httpHeaders)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(inputStream));

    }
}

