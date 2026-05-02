package com.crud.api.command.note;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.NoteController;
import com.crud.dto.NoteResponse;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

@Slf4j
public class FindNoteByIdCommand implements Command {
    private final NoteController noteController;
    private final ConsoleInput consoleInput;
    private final UserMapper userMapper;

    public FindNoteByIdCommand(NoteController noteController, ConsoleInput consoleInput) {
        this.noteController = noteController;
        this.consoleInput = consoleInput;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID заметки: ");
        try {
            NoteResponse note = noteController.findNoteById(id);
            if (log.isInfoEnabled()) {
                log.info("""
                        Найдена заметка:
                           ID: {}
                           Содержание: {}
                           Создана: {}
                           Обновлена: {}
                        """, note.id(), note.content(),
                        userMapper.formatDateTime(note.createdAt()),
                        userMapper.formatDateTime(note.updatedAt()));
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
