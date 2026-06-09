package com.crud.service;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис заметок.
 */
public interface NoteService {

    NoteResponse createNote(Long userId, NoteRequest request);
    NoteResponse findNoteById(Long userId, Long id);
    Page<NoteResponse> findNotesByUserId(Long userId, Pageable pageable);
    NoteResponse updateNote(Long userId, Long id, NoteRequest request);
    void deleteNote(Long userId, Long id);
}
