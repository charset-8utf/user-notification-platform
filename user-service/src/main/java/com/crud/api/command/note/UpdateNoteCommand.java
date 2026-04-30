package com.crud.api.command.note;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.NoteController;
import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;

@Slf4j
public class UpdateNoteCommand implements Command {
    private final NoteController noteController;
    private final ConsoleInput consoleInput;

    public UpdateNoteCommand(NoteController noteController, ConsoleInput consoleInput) {
        this.noteController = noteController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID заметки для обновления: ");
        try {
            NoteResponse existing = noteController.findNoteById(id);
            log.info("Текущее содержание: {}", existing.content());
            String newContent = consoleInput.readString("Введите новое содержание (Enter - оставить без изменений): ", existing.content());
            NoteResponse updated = noteController.updateNote(id, new NoteRequest(newContent));
            if (log.isInfoEnabled()) {
                log.info("Заметка ID {} обновлена", updated.id());
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}