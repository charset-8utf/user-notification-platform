package com.crud.api.command.note;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.NoteController;
import com.crud.dto.NoteResponse;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListNotesCommandTest extends ConsoleCommandTest {

    @Mock
    private NoteController noteController;

    @Test
    void execute_WhenNotesExist_ShouldCallController() {
        provideInput("1\n0\n");
        ListNotesCommand command = new ListNotesCommand(noteController, getConsoleInput());

        NoteResponse note1 = new NoteResponse(1L, "Note 1", LocalDateTime.now(), LocalDateTime.now());
        NoteResponse note2 = new NoteResponse(2L, "Note 2", LocalDateTime.now(), LocalDateTime.now());
        Page<NoteResponse> page = new Page<>(List.of(note1, note2), 2, 0, 5);

        when(noteController.findNotesByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        command.execute();

        verify(noteController).findNotesByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void execute_WhenNoNotes_ShouldCallController() {
        provideInput("1\n");
        ListNotesCommand command = new ListNotesCommand(noteController, getConsoleInput());
        Page<NoteResponse> emptyPage = new Page<>(List.of(), 0, 0, 5);

        when(noteController.findNotesByUserId(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        command.execute();

        verify(noteController).findNotesByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\n");
        ListNotesCommand command = new ListNotesCommand(noteController, getConsoleInput());
        when(noteController.findNotesByUserId(eq(1L), any(Pageable.class))).thenThrow(new RuntimeException("User not found"));

        assertDoesNotThrow(command::execute);
    }
}
