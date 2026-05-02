package com.crud.api.command.note;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.api.command.Confirmation;
import com.crud.controller.NoteController;

@Slf4j
public class DeleteNoteCommand implements Command {
    private final NoteController noteController;
    private final ConsoleInput consoleInput;

    public DeleteNoteCommand(NoteController noteController, ConsoleInput consoleInput) {
        this.noteController = noteController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID заметки для удаления: ");
        String confirm = consoleInput.readString("Вы уверены? (y/n): ", "n");
        if (!Confirmation.isConfirmed(confirm)) {
            log.info("Удаление отменено.");
            return;
        }
        try {
            noteController.deleteNote(id);
            log.info("Заметка с ID {} удалена", id);
        } catch (RuntimeException e) {
            log.error("Ошибка удаления: {}", e.getMessage(), e);
        }
    }
}
