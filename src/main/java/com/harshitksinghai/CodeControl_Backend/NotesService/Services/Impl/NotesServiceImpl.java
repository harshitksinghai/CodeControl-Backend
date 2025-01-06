package com.harshitksinghai.CodeControl_Backend.NotesService.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO.NotesDownloadResponseDTO;
import com.harshitksinghai.CodeControl_Backend.NotesService.Enums.NotesStatus;
import com.harshitksinghai.CodeControl_Backend.NotesService.Models.Notes;
import com.harshitksinghai.CodeControl_Backend.NotesService.Repositories.NotesRepository;
import com.harshitksinghai.CodeControl_Backend.NotesService.Services.NotesService;
import com.harshitksinghai.CodeControl_Backend.VideoService.Models.Video;
import com.harshitksinghai.CodeControl_Backend.VideoService.Repositories.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotesServiceImpl implements NotesService {
    private final Logger LOG = LoggerFactory.getLogger(NotesServiceImpl.class);


    @Autowired
    NotesRepository notesRepository;

    @Autowired
    VideoRepository videoRepository;

    @Value("${files.upload.notes}")
    String notesStoragePath;

    @Override
    public ResponseEntity<CommonResponseDTO> uploadNotesFile(String videoId, MultipartFile notesFile) {
        CommonResponseDTO response = new CommonResponseDTO();

        try {
            // Validate file
            if (notesFile.isEmpty()) {
                response.setMessage("File is empty");
                response.setStatus(false);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (!isValidFileType(notesFile.getContentType())) {
                response.setMessage("Invalid file type. Only PDF files are allowed");
                response.setStatus(false);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Find associated video
            Video video = videoRepository.findByVideoId(videoId)
                    .orElseThrow(() -> new IllegalArgumentException("Video not found with ID: " + videoId));

            // Check if notes already exist for this video
            if (notesRepository.existsByVideo(video)) {
                response.setMessage("Notes already exist for this video. Please delete existing notes first.");
                response.setStatus(false);
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(notesStoragePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String notesId = UUID.randomUUID().toString();
            String fileName = notesId + "_" + notesFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file to filesystem
            Files.copy(notesFile.getInputStream(), filePath);

            // Create notes entity
            Notes notes = new Notes();
            notes.setNotesId(notesId);
            notes.setTitle(notesFile.getOriginalFilename());
            notes.setFilePath(filePath.toString());
            notes.setContentType(notesFile.getContentType());
            notes.setFileSize(notesFile.getSize());
            notes.setStatus(NotesStatus.PENDING_APPROVAL);
            notes.setUploadDate(LocalDateTime.now());
            notes.setVideo(video);

            notesRepository.save(notes);

            response.setMessage("Notes uploaded successfully");
            response.setStatus(true);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            response.setMessage(e.getMessage());
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOG.error("Error while uploading file", e);
            response.setMessage("Failed to upload notes");
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private boolean isValidFileType(String contentType) {
        return contentType != null && contentType.equals("application/pdf");
    }

    @Override
    public boolean deleteNotesFile(String notesId) {
        try {
            Notes notes = notesRepository.findByNotesId(notesId)
                    .orElseThrow(() -> new IllegalArgumentException("Notes not found with ID: " + notesId));

            // Delete file from filesystem
            Path filePath = Paths.get(notes.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete from database
            notesRepository.delete(notes);

            return true;
        } catch (Exception e) {
            LOG.error("Error while deleting notes", e);
            return false;
        }
    }

    @Override
    public NotesDownloadResponseDTO downloadNotes(String notesId) {
        try {
            Notes notes = notesRepository.findByNotesId(notesId)
                    .orElseThrow(() -> new IllegalArgumentException("Notes not found with ID: " + notesId));

            // Check if notes are approved for download
            if (notes.getStatus() != NotesStatus.APPROVED) {
                throw new IllegalStateException("Notes are not approved for download");
            }

            Path filePath = Paths.get(notes.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new IllegalStateException("File not found: " + notes.getFilePath());
            }

            return new NotesDownloadResponseDTO(
                    resource,
                    notes.getContentType(),
                    notes.getTitle()
            );

        } catch (MalformedURLException e) {
            LOG.error("Error while downloading notes", e);
            throw new RuntimeException("Error downloading notes file", e);
        }
    }
}
