package com.harshitksinghai.CodeControl_Backend.NotesService.Models;

import com.harshitksinghai.CodeControl_Backend.NotesService.Enums.NotesStatus;
import com.harshitksinghai.CodeControl_Backend.VideoService.Models.Video;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notes")
public class Notes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String notesId;

    private String title;
    private String filePath;
    private String contentType;
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private NotesStatus status;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @OneToOne
    @JoinColumn(name = "video_id")
    private Video video;

//    @ManyToOne
//    @JoinColumn(name = "uploaded_by")
//    private User uploadedBy;
}
