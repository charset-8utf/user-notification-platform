package com.crud.api.command.note;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.NoteController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteNoteCommandTest extends ConsoleCommandTest {

    @Mock
    private NoteController noteController;

    @Test
    void execute_WhenConfirmed_ShouldDeleteNote() {
        provideInput("1\ny\n");
        DeleteNoteCommand command = new DeleteNoteCommand(noteController, getConsoleInput());
        doNothing().when(noteController).deleteNote(1L);

        command.execute();

        verify(noteController).deleteNote(1L);
    }

    @Test
    void execute_WhenNotConfirmed_ShouldNotDelete() {
        provideInput("1\nn\n");
        DeleteNoteCommand command = new DeleteNoteCommand(noteController, getConsoleInput());

        command.execute();

        verify(noteController, never()).deleteNote(anyLong());
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\ny\n");
        DeleteNoteCommand command = new DeleteNoteCommand(noteController, getConsoleInput());
        doThrow(new RuntimeException("Note not found")).when(noteController).deleteNote(1L);

        assertDoesNotThrow(command::execute);
    }
}
