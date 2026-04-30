package com.crud.api.command.note;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.NoteController;
import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateNoteCommandTest extends ConsoleCommandTest {

    @Mock
    private NoteController noteController;

    @Test
    void execute_ShouldCreateNote() {
        provideInput("1\nTest note content\n");
        CreateNoteCommand command = new CreateNoteCommand(noteController, getConsoleInput());
        when(noteController.createNote(eq(1L), any(NoteRequest.class)))
                .thenReturn(new NoteResponse(1L, "Test note content", null, null));

        command.execute();

        verify(noteController).createNote(eq(1L), any(NoteRequest.class));
    }

    @Test
    void execute_WhenContentIsBlank_ShouldNotCallController() {
        provideInput("1\n\n");
        CreateNoteCommand command = new CreateNoteCommand(noteController, getConsoleInput());

        command.execute();

        verify(noteController, never()).createNote(anyLong(), any(NoteRequest.class));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\nTest content\n");
        CreateNoteCommand command = new CreateNoteCommand(noteController, getConsoleInput());
        when(noteController.createNote(eq(1L), any(NoteRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        assertDoesNotThrow(command::execute);
    }
}
