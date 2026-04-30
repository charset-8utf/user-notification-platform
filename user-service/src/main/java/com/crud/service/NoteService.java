package com.crud.service;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.dto.Page;
import com.crud.dto.Pageable;

/**
 * Сервис заметок.
 */
public interface NoteService {

    NoteResponse createNote(Long userId, NoteRequest request);
    NoteResponse findNoteById(Long id);
    Page<NoteResponse> findNotesByUserId(Long userId, Pageable pageable);
    NoteResponse updateNote(Long id, NoteRequest request);
    void deleteNote(Long id);
}
