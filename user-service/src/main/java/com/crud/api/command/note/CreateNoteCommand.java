package com.crud.api.command.note;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.NoteController;
import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;

@Slf4j
public class CreateNoteCommand implements Command {
    private final NoteController noteController;
    private final ConsoleInput consoleInput;

    public CreateNoteCommand(NoteController noteController, ConsoleInput consoleInput) {
        this.noteController = noteController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long userId = consoleInput.readLong("Введите ID пользователя, которому принадлежит заметка: ");
        String content = consoleInput.readString("Введите текст заметки: ", "");
        if (content.isBlank()) {
            log.error("Текст заметки не может быть пустым.");
            return;
        }
        try {
            NoteResponse note = noteController.createNote(userId, new NoteRequest(content));
            log.info("Заметка создана! ID: {}", note.id());
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
