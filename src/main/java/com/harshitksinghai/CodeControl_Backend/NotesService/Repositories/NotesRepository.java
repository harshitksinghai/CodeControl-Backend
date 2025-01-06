package com.harshitksinghai.CodeControl_Backend.NotesService.Repositories;

import com.harshitksinghai.CodeControl_Backend.NotesService.Models.Notes;
import com.harshitksinghai.CodeControl_Backend.VideoService.Models.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotesRepository extends JpaRepository<Notes, Long> {
    boolean existsByVideo(Video video);

    Optional<Notes> findByNotesId(String notesId);
}
