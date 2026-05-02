package com.crud.controller;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.service.NoteService;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация контроллера заметок.
 */
@Slf4j
public class NoteControllerImpl implements NoteController {

    private final NoteService noteService;

    public NoteControllerImpl(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public NoteResponse createNote(Long userId, NoteRequest request) {
        log.debug("Контроллер: создание заметки для пользователя ID: {}", userId);
        return noteService.createNote(userId, request);
    }

    @Override
    public NoteResponse findNoteById(Long id) {
        log.debug("Контроллер: поиск заметки по ID: {}", id);
        return noteService.findNoteById(id);
    }

    @Override
    public Page<NoteResponse> findNotesByUserId(Long userId, Pageable pageable) {
        log.debug("Контроллер: поиск заметок пользователя ID: {} с пагинацией: page={}, size={}", userId, pageable.page(), pageable.size());
        return noteService.findNotesByUserId(userId, pageable);
    }

    @Override
    public NoteResponse updateNote(Long id, NoteRequest request) {
        log.debug("Контроллер: обновление заметки ID: {}", id);
        return noteService.updateNote(id, request);
    }

    @Override
    public void deleteNote(Long id) {
        log.debug("Контроллер: удаление заметки ID: {}", id);
        noteService.deleteNote(id);
    }
}
