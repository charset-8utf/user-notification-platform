package com.crud.controller;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.dto.Page;
import com.crud.dto.Pageable;

/**
 * Контроллер заметок.
 */
public interface NoteController {

    /**
     * Создаёт заметку.
     */
    NoteResponse createNote(Long userId, NoteRequest request);

    /**
     * Находит заметку по ID.
     */
    NoteResponse findNoteById(Long id);

    /**
     * Возвращает страницу заметок пользователя.
     */
    Page<NoteResponse> findNotesByUserId(Long userId, Pageable pageable);

    /**
     * Обновляет заметку.
     */
    NoteResponse updateNote(Long id, NoteRequest request);

    /**
     * Удаляет заметку.
     */
    void deleteNote(Long id);
}
