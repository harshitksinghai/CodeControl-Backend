package com.harshitksinghai.CodeControl_Backend.VideoService.Models;

import com.harshitksinghai.CodeControl_Backend.AuthService.Models.User;
import com.harshitksinghai.CodeControl_Backend.VideoService.Enums.VideoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String videoId;

    private String filePath;
    private String contentType;
    private String title;

    @Enumerated(EnumType.STRING)
    private VideoStatus status;

    @ManyToOne
    private User approvedBy; // Who approved the video

    @ManyToOne
    private User uploadedBy; // Who uploaded the video
}
