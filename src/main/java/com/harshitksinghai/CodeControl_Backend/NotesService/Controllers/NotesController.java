package com.harshitksinghai.CodeControl_Backend.NotesService.Controllers;

import com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO.CommonResponseDTO;
import com.harshitksinghai.CodeControl_Backend.NotesService.DTOs.ResponseDTO.NotesDownloadResponseDTO;
import com.harshitksinghai.CodeControl_Backend.NotesService.Services.NotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

    @Autowired
    NotesService notesService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('RESOURCE_CREATE')")
    public ResponseEntity<CommonResponseDTO> uploadNotesFile(@RequestParam("videoId") String videoId, @RequestParam("file") MultipartFile notesFile){
        return notesService.uploadNotesFile(videoId, notesFile);
    }

    @DeleteMapping("/delete/{notesId}")
    @PreAuthorize("hasAuthority('RESOURCE_DELETE')")
    public ResponseEntity<CommonResponseDTO> deleteNotesFile(@PathVariable("notesId") String notesId){
        CommonResponseDTO commonResponseDTO = new CommonResponseDTO();
        boolean res = notesService.deleteNotesFile(notesId);
        commonResponseDTO.setStatus(res);
        if(res){
            commonResponseDTO.setMessage("Notes file deleted successfully!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.OK);
        }
        else{
            commonResponseDTO.setMessage("Unable to delete notes file!");
            return new ResponseEntity<>(commonResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download/{notesId}")
    @PreAuthorize("hasAuthority('RESOURCE_READ')")
    public ResponseEntity<Resource> downloadNotes(@PathVariable("notesId") String notesId) {
        NotesDownloadResponseDTO downloadResponse = notesService.downloadNotes(notesId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadResponse.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadResponse.getFileName() + "\"")
                .body(downloadResponse.getResource());
    }
}
