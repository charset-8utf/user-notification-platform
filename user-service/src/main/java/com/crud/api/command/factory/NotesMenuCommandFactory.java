package com.crud.api.command.factory;

import com.crud.api.ConsoleInput;
import com.crud.api.MenuState;
import com.crud.api.command.Command;
import com.crud.api.command.note.CreateNoteCommand;
import com.crud.api.command.note.DeleteNoteCommand;
import com.crud.api.command.note.FindNoteByIdCommand;
import com.crud.api.command.note.ListNotesCommand;
import com.crud.api.command.note.UpdateNoteCommand;
import com.crud.controller.NoteController;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика команд управления заметками.
 */
public class NotesMenuCommandFactory implements MenuCommandFactory {
    private final NoteController noteController;
    private final ConsoleInput consoleInput;

    public NotesMenuCommandFactory(NoteController noteController, ConsoleInput consoleInput) {
        this.noteController = noteController;
        this.consoleInput = consoleInput;
    }

    @Override
    public MenuState state() {
        return MenuState.NOTES;
    }

    @Override
    public Map<Integer, Command> createCommands() {
        Map<Integer, Command> commands = new HashMap<>();
        commands.put(1, new CreateNoteCommand(noteController, consoleInput));
        commands.put(2, new FindNoteByIdCommand(noteController, consoleInput));
        commands.put(3, new ListNotesCommand(noteController, consoleInput));
        commands.put(4, new UpdateNoteCommand(noteController, consoleInput));
        commands.put(5, new DeleteNoteCommand(noteController, consoleInput));
        return commands;
    }
}
