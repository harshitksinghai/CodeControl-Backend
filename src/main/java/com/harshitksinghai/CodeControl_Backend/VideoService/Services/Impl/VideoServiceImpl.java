package com.harshitksinghai.CodeControl_Backend.VideoService.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.AuthService.Models.User;
import com.harshitksinghai.CodeControl_Backend.AuthService.Repositories.UserRepository;
import com.harshitksinghai.CodeControl_Backend.VideoService.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.VideoService.Enums.VideoStatus;
import com.harshitksinghai.CodeControl_Backend.VideoService.Models.Video;
import com.harshitksinghai.CodeControl_Backend.VideoService.Repositories.VideoRepository;
import com.harshitksinghai.CodeControl_Backend.VideoService.Services.VideoService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class VideoServiceImpl implements VideoService {
    private final Logger LOG = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    VideoRepository videoRepository;

    @Value("${files.upload.video}")
    String videoStoragePath;

    @Value("${files.upload.video-hls}")
    String processedVideoStoragePath;

    @PostConstruct
    public void init(){
        File file = new File(videoStoragePath);
        if(!file.exists()){
            file.mkdir();
            System.out.println("Folder created!");
        }
        else{
            System.out.println("Folder already created!");
        }

        try {
            Files.createDirectories(Path.of(processedVideoStoragePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<CommonResponseDTO> uploadVideo(String title, MultipartFile videoFile) {
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        try{
            Files.createDirectories(Path.of(videoStoragePath));
            Files.createDirectories(Path.of(processedVideoStoragePath));

            String videoFileName = videoFile.getOriginalFilename();
            String contentType = videoFile.getContentType();
            InputStream inputStream = videoFile.getInputStream();

            String cleanVideoFileName = StringUtils.cleanPath(videoFileName);
            String cleanVideoUploadDIR = StringUtils.cleanPath(videoStoragePath);
            Path path = Paths.get(cleanVideoUploadDIR, cleanVideoFileName);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            Video video = new Video();
            video.setVideoId(UUID.randomUUID().toString());
            video.setStatus(VideoStatus.PENDING_PROCESSING);
            video.setTitle(title);
            video.setContentType(contentType);
            //video.setUploadedBy(userOpt.get());
            video.setFilePath(path.toString());
            videoRepository.save(video);

            processVideo(video.getVideoId());

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
    public void processVideo(String videoId) {
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found for provided videoId: " + videoId));

        String filePath = video.getFilePath();

        String output360p = processedVideoStoragePath + "/" + videoId + "/360p/";
        String output480p = processedVideoStoragePath + "/" + videoId + "/480p/";
        String output720p = processedVideoStoragePath + "/" + videoId + "/720p/";
        String output1080p = processedVideoStoragePath + "/" + videoId + "/1080p/";

        try{
            Files.createDirectories(Path.of(output360p));
            Files.createDirectories(Path.of(output480p));
            Files.createDirectories(Path.of(output720p));
            Files.createDirectories(Path.of(output1080p));

            // FFmpeg command for 360p
            String[] cmd360p = {
                    "ffmpeg", "-i", filePath,
                    "-profile:v", "baseline", "-level", "3.0",
                    "-s", "640x360", "-start_number", "0",
                    "-hls_time", "10", "-hls_list_size", "0",
                    "-f", "hls", output360p + "playlist.m3u8"
            };

            // FFmpeg command for 480p
            String[] cmd480p = {
                    "ffmpeg", "-i", filePath,
                    "-profile:v", "baseline", "-level", "3.0",
                    "-s", "854x480", "-start_number", "0",
                    "-hls_time", "10", "-hls_list_size", "0",
                    "-f", "hls", output480p + "playlist.m3u8"
            };

            // FFmpeg command for 720p
            String[] cmd720p = {
                    "ffmpeg", "-i", filePath,
                    "-profile:v", "main", "-level", "3.1",
                    "-s", "1280x720", "-start_number", "0",
                    "-hls_time", "10", "-hls_list_size", "0",
                    "-f", "hls", output720p + "playlist.m3u8"
            };

            // FFmpeg command for 1080p
            String[] cmd1080p = {
                    "ffmpeg", "-i", filePath,
                    "-profile:v", "main", "-level", "4.0",
                    "-s", "1920x1080", "-start_number", "0",
                    "-hls_time", "10", "-hls_list_size", "0",
                    "-f", "hls", output1080p + "playlist.m3u8"
            };

            // Execute FFmpeg commands
            ProcessBuilder pb360p = new ProcessBuilder(cmd360p);
            ProcessBuilder pb480p = new ProcessBuilder(cmd480p);
            ProcessBuilder pb720p = new ProcessBuilder(cmd720p);
            ProcessBuilder pb1080p = new ProcessBuilder(cmd1080p);

            // Start all processes
            Process p360p = pb360p.start();
            Process p480p = pb480p.start();
            Process p720p = pb720p.start();
            Process p1080p = pb1080p.start();

            // Wait for all processes to complete
            int exitCode360p = p360p.waitFor();
            LOG.info("360p transcoding completed for videoId: {} with exit code: {}", videoId, exitCode360p);
            int exitCode480p = p480p.waitFor();
            LOG.info("480p transcoding completed for videoId: {} with exit code: {}", videoId, exitCode480p);
            int exitCode720p = p720p.waitFor();
            LOG.info("720p transcoding completed for videoId: {} with exit code: {}", videoId, exitCode720p);
            int exitCode1080p = p1080p.waitFor();
            LOG.info("1080p transcoding completed for videoId: {} with exit code: {}", videoId, exitCode1080p);

            // Check if all processes completed successfully
            if (exitCode360p != 0 || exitCode480p != 0 || exitCode720p != 0 || exitCode1080p != 0) {
                throw new RuntimeException("FFmpeg process failed");
            }

            // Create master playlist
            createMasterPlaylist(videoId);

            video.setStatus(VideoStatus.PENDING_APPROVAL);
            videoRepository.save(video);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error processing video: " + e.getMessage());
        }
    }
    private void createMasterPlaylist(String videoId) throws IOException {
        String masterPlaylistContent = "#EXTM3U\n" +
                "#EXT-X-VERSION:3\n" +
                "\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360\n" +
                "360p/playlist.m3u8\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=1400000,RESOLUTION=854x480\n" +
                "480p/playlist.m3u8\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=2800000,RESOLUTION=1280x720\n" +
                "720p/playlist.m3u8\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080\n" +
                "1080p/playlist.m3u8";

        Path masterPlaylistPath = Path.of(processedVideoStoragePath + "/" + videoId + "/master.m3u8");
        Files.writeString(masterPlaylistPath, masterPlaylistContent);
        LOG.info("Master Playlist created for videoId: {}", videoId);
    }

    @Override
    public ResponseEntity<Resource> streamVideoHLSAdaptive(String videoId) {
        LOG.info("Received request for adaptive stream: videoId={}", videoId);
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found for provided videoId: " + videoId));

        // Return master playlist for adaptive streaming
        String masterPlaylistPath = processedVideoStoragePath + "/" + videoId + "/master.m3u8";
        Resource masterPlaylist = new FileSystemResource(masterPlaylistPath);

        if (!masterPlaylist.exists()) {
            LOG.error("Master playlist not found for video ID: {}", videoId);
            return ResponseEntity.notFound().build();
        }

        LOG.info("Serving master playlist for video ID: {} for adaptive streaming", videoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-mpegURL"))
                //.header("Access-Control-Allow-Origin", "*") // For CORS support
                .header("Cache-Control", "no-cache")
                .body(masterPlaylist);
    }

    @Override
    public ResponseEntity<Resource> streamVideoHLSSpecificQuality(String videoId, String quality, String segmentPath) {
        LOG.info("Received request for specific quality stream: videoId={}", videoId);
        //Video video = videoRepository.findByVideoId(videoId)
        //        .orElseThrow(() -> new RuntimeException("Video not found for provided videoId: " + videoId));

        // Validate quality parameter
        if (!isValidQuality(quality)) {
            LOG.error("Invalid quality '{}' for videoId '{}'", quality, videoId);
            return ResponseEntity.badRequest().build();
        }

        // Construct the full path based on whether it's a playlist or segment request
        String fullPath;
        String contentType;

        if (segmentPath == null) {
            // Return quality-specific playlist
            fullPath = processedVideoStoragePath + "/" + videoId + "/" + quality + "/playlist.m3u8";
            contentType = "application/x-mpegURL";
            LOG.info("Serving {} quality playlist for video ID: {}", quality, videoId);
        } else {
            // Return specific segment
            fullPath = processedVideoStoragePath + "/" + videoId + "/" + quality + "/" + segmentPath;
            contentType = "video/MP2T";
            LOG.info("Serving {} quality segment: {} for video ID: {}", quality, segmentPath, videoId);
        }

        Resource resource = new FileSystemResource(fullPath);
        if (!resource.exists()) {
            LOG.error("Resource not found: {} for video ID: {}", fullPath, videoId);
            return ResponseEntity.notFound().build();
        }

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(resource.contentLength())
                    //.header("Access-Control-Allow-Origin", "*")
                    .header("Cache-Control", "public, max-age=3600") // Cache segments for 1 hour
                    .body(resource);
        } catch (IOException e) {
            LOG.error("Error reading resource: {} for video ID: {}", fullPath, videoId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    private boolean isValidQuality(String quality) {
        return Arrays.asList("360p", "480p", "720p", "1080p").contains(quality);
    }

    @Override
    @Transactional
    public boolean deleteVideo(String videoId) {
        LOG.info("Inside deleteVideo method in VideoServiceImpl");
        if(deleteVideoMetaData(videoId)){
            if(deleteVideoFile(videoId)){
                return true;
            }
            else{
                LOG.info("Deleted video meta data but unable to delete video file!");
                return false;
            }
        }
        else {
            LOG.info("Unable to delete video meta data!");
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteVideoMetaData(String videoId) {
        try {
            videoRepository.deleteByVideoId(videoId);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteVideoFile(String videoId){
        File videoDirectory = new File(processedVideoStoragePath + "/" + videoId);
        if (videoDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(videoDirectory);
                return true;
            } catch (IOException e) {
                LOG.error("Error deleting video directory for videoId {}: {}", videoId, e.getMessage(), e);
                return false;
            }
        }
        LOG.debug("Video directory not found for videoId: {}", videoId);
        return false;
    }

    @Override
    public ResponseEntity<CommonResponseDTO> updateVideo(String videoId, String title, MultipartFile videoFile) {
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        try {
            // Retrieve the video from the database
            Video video = videoRepository.findByVideoId(videoId)
                    .orElseThrow(() -> new IllegalArgumentException("Video not found"));

            // Update metadata if provided
            if (title != null && !title.isBlank()) {
                video.setTitle(title);
            }

            // Update video file if provided
            if (videoFile != null && !videoFile.isEmpty()) {
                Files.createDirectories(Path.of(videoStoragePath));

                String videoFileName = StringUtils.cleanPath(videoFile.getOriginalFilename());
                Path filePath = Paths.get(videoStoragePath, videoFileName);

                // Replace the existing file
                Files.copy(videoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Update file path and content type
                video.setFilePath(filePath.toString());
                video.setContentType(videoFile.getContentType());
                video.setStatus(VideoStatus.PENDING_PROCESSING);

                // Process the new video
                processVideo(videoId);
            }

            // Save updated video to the database
            videoRepository.save(video);

            commonResponseDTO.setStatus(true);
            commonResponseDTO.setMessage("Video updated successfully!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            commonResponseDTO.setStatus(false);
            commonResponseDTO.setMessage("Video update failed!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

