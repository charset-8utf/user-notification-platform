package com.crud.controller;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.service.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteControllerImplTest {

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteControllerImpl noteController;

    @Test
    void createNote_ShouldDelegateToService() {
        NoteRequest request = new NoteRequest("Test content");
        NoteResponse expected = new NoteResponse(1L, "Test content", LocalDateTime.now(), LocalDateTime.now());
        when(noteService.createNote(1L, request)).thenReturn(expected);

        NoteResponse actual = noteController.createNote(1L, request);

        assertEquals(expected, actual);
        verify(noteService).createNote(1L, request);
    }

    @Test
    void findNoteById_ShouldDelegateToService() {
        NoteResponse expected = new NoteResponse(1L, "Test content", LocalDateTime.now(), LocalDateTime.now());
        when(noteService.findNoteById(1L)).thenReturn(expected);

        NoteResponse actual = noteController.findNoteById(1L);

        assertEquals(expected, actual);
        verify(noteService).findNoteById(1L);
    }

    @Test
    void findNotesByUserId_ShouldDelegateToService() {
        Page<NoteResponse> expected = new Page<>(List.of(
                new NoteResponse(1L, "Note 1", LocalDateTime.now(), LocalDateTime.now()),
                new NoteResponse(2L, "Note 2", LocalDateTime.now(), LocalDateTime.now())
        ), 2, 0, 5);
        when(noteService.findNotesByUserId(1L, Pageable.of(0, 5))).thenReturn(expected);

        Page<NoteResponse> actual = noteController.findNotesByUserId(1L, Pageable.of(0, 5));

        assertEquals(expected, actual);
        verify(noteService).findNotesByUserId(1L, Pageable.of(0, 5));
    }

    @Test
    void updateNote_ShouldDelegateToService() {
        NoteRequest request = new NoteRequest("Updated content");
        NoteResponse expected = new NoteResponse(1L, "Updated content", LocalDateTime.now(), LocalDateTime.now());
        when(noteService.updateNote(1L, request)).thenReturn(expected);

        NoteResponse actual = noteController.updateNote(1L, request);

        assertEquals(expected, actual);
        verify(noteService).updateNote(1L, request);
    }

    @Test
    void deleteNote_ShouldDelegateToService() {
        doNothing().when(noteService).deleteNote(1L);

        noteController.deleteNote(1L);

        verify(noteService).deleteNote(1L);
    }
}
