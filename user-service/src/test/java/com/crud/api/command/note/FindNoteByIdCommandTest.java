package com.crud.api.command.note;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.NoteController;
import com.crud.dto.NoteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindNoteByIdCommandTest extends ConsoleCommandTest {

    @Mock
    private NoteController noteController;

    @Test
    void execute_WhenNoteExists_ShouldCallController() {
        provideInput("1\n");
        FindNoteByIdCommand command = new FindNoteByIdCommand(noteController, getConsoleInput());
        when(noteController.findNoteById(1L))
                .thenReturn(new NoteResponse(1L, "Test content", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(noteController).findNoteById(1L);
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("999\n");
        FindNoteByIdCommand command = new FindNoteByIdCommand(noteController, getConsoleInput());
        when(noteController.findNoteById(999L)).thenThrow(new RuntimeException("Note not found"));

        assertDoesNotThrow(command::execute);
    }
}
