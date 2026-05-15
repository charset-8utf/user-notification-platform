package com.crud.controller;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            @PathVariable Long userId,
            @Valid @RequestBody NoteRequest request) {
        NoteResponse response = noteService.createNote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> findNoteById(
            @PathVariable Long userId,
            @PathVariable Long id) {
        NoteResponse response = noteService.findNoteById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<NoteResponse>> findNotesByUserId(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<NoteResponse> page = noteService.findNotesByUserId(userId, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long userId,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        NoteResponse response = noteService.updateNote(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long userId,
            @PathVariable Long id) {
        noteService.deleteNote(userId, id);
        return ResponseEntity.noContent().build();
    }
}
