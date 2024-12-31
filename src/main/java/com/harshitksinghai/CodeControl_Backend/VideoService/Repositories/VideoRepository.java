package com.harshitksinghai.CodeControl_Backend.VideoService.Repositories;

import com.harshitksinghai.CodeControl_Backend.VideoService.Models.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByVideoId(String videoId);
}
