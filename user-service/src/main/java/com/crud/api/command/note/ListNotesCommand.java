package com.crud.api.command.note;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import com.crud.api.ConsoleInput;
import com.crud.api.command.PagedConsoleSupport;
import com.crud.api.command.PagedListCommand;
import com.crud.controller.NoteController;
import com.crud.dto.NoteResponse;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

/**
 * Команда для отображения списка заметок пользователя с пагинацией.
 */
@Slf4j
public class ListNotesCommand extends PagedListCommand<NoteResponse> {

    private final NoteController noteController;
    private final UserMapper userMapper;
    private long userId;

    public ListNotesCommand(NoteController noteController, ConsoleInput consoleInput) {
        this(noteController, consoleInput, DEFAULT_PAGE_SIZE);
    }

    public ListNotesCommand(NoteController noteController, ConsoleInput consoleInput, int pageSize) {
        this(noteController, consoleInput, pageSize, new PagedConsoleSupport());
    }

    public ListNotesCommand(NoteController noteController,
                            ConsoleInput consoleInput,
                            int pageSize,
                            PagedConsoleSupport pagedConsoleSupport) {
        super(consoleInput, pageSize, pagedConsoleSupport);
        this.noteController = noteController;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    public void execute() {
        userId = consoleInput.readLong("Введите ID пользователя, заметки которого хотите посмотреть: ");
        super.execute();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected Page<NoteResponse> fetchPage(Pageable pageable) {
        return noteController.findNotesByUserId(userId, pageable);
    }

    @Override
    protected void displayHeader(int currentPage, Page<NoteResponse> page) {
        log.info("Заметки пользователя ID {} (страница {} из {}):", userId, currentPage + 1, page.totalPages());
    }

    @Override
    protected void displayContent(Page<NoteResponse> page) {
        if (log.isInfoEnabled()) {
            page.content().forEach(note ->
                log.info("   ID: {} | {} | Создана: {} | Обновлена: {}",
                        note.id(), note.content(),
                        userMapper.formatDateTime(note.createdAt()),
                        userMapper.formatDateTime(note.updatedAt())));
        }
    }
}
