package com.crud.controller;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.security.SanitizedJsonResponses;
import com.crud.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users/{userId}/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final SanitizedJsonResponses responses;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            @PathVariable Long userId,
            @Valid @RequestBody NoteRequest request) {
        return responses.created(noteService.createNote(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> findNoteById(
            @PathVariable Long userId,
            @PathVariable Long id) {
        return responses.ok(noteService.findNoteById(userId, id));
    }

    @GetMapping
    public ResponseEntity<Page<NoteResponse>> findNotesByUserId(
            @PathVariable Long userId,
            Pageable pageable) {
        return responses.okNotes(noteService.findNotesByUserId(userId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long userId,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        return responses.ok(noteService.updateNote(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long userId,
            @PathVariable Long id) {
        noteService.deleteNote(userId, id);
        return responses.noContent();
    }
}
