package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Note;
import java.util.Optional;

/**
 * Репозиторий заметок.
 */
public interface NoteRepository {

    Note save(Note note);
    Optional<Note> findById(Long id);
    Page<Note> findAll(Pageable pageable);
    Note update(Note note);
    void deleteById(Long id);
    Page<Note> findByUserId(Long userId, Pageable pageable);
}
