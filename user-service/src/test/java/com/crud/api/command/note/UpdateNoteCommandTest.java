package com.crud.api.command.note;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.NoteController;
import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateNoteCommandTest extends ConsoleCommandTest {

    @Mock
    private NoteController noteController;

    @Test
    void execute_ShouldUpdateNote() {
        provideInput("1\nNew content\n");
        UpdateNoteCommand command = new UpdateNoteCommand(noteController, getConsoleInput());
        when(noteController.findNoteById(1L))
                .thenReturn(new NoteResponse(1L, "Old content", LocalDateTime.now(), LocalDateTime.now()));
        when(noteController.updateNote(eq(1L), any(NoteRequest.class)))
                .thenReturn(new NoteResponse(1L, "New content", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(noteController).updateNote(eq(1L), any(NoteRequest.class));
    }

    @Test
    void execute_WhenBlankContent_ShouldKeepExisting() {
        provideInput("1\n\n");
        UpdateNoteCommand command = new UpdateNoteCommand(noteController, getConsoleInput());
        when(noteController.findNoteById(1L))
                .thenReturn(new NoteResponse(1L, "Existing content", LocalDateTime.now(), LocalDateTime.now()));
        when(noteController.updateNote(eq(1L), any(NoteRequest.class)))
                .thenReturn(new NoteResponse(1L, "Existing content", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(noteController).updateNote(eq(1L), argThat(req -> req.content().equals("Existing content")));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("999\n");
        UpdateNoteCommand command = new UpdateNoteCommand(noteController, getConsoleInput());
        when(noteController.findNoteById(999L)).thenThrow(new RuntimeException("Note not found"));

        assertDoesNotThrow(command::execute);
    }
}
