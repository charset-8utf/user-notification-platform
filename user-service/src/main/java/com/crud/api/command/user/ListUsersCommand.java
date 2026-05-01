package com.crud.api.command.user;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import com.crud.api.ConsoleInput;
import com.crud.api.command.PagedConsoleSupport;
import com.crud.api.command.PagedListCommand;
import com.crud.controller.UserController;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.UserResponse;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

/**
 * Команда для отображения списка пользователей с пагинацией.
 */
@Slf4j
public class ListUsersCommand extends PagedListCommand<UserResponse> {

    private final UserController controller;
    private final UserMapper userMapper;

    public ListUsersCommand(UserController controller, ConsoleInput consoleInput) {
        this(controller, consoleInput, DEFAULT_PAGE_SIZE);
    }

    public ListUsersCommand(UserController controller, ConsoleInput consoleInput, int pageSize) {
        this(controller, consoleInput, pageSize, new PagedConsoleSupport());
    }

    public ListUsersCommand(UserController controller,
                            ConsoleInput consoleInput,
                            int pageSize,
                            PagedConsoleSupport pagedConsoleSupport) {
        super(consoleInput, pageSize, pagedConsoleSupport);
        this.controller = controller;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected void displayHeader(int currentPage, Page<UserResponse> page) {
        log.info("Пользователи (страница {} из {}):", currentPage + 1, page.totalPages());
    }

    @Override
    protected Page<UserResponse> fetchPage(Pageable pageable) {
        return controller.findAllUsers(pageable);
    }

    @Override
    protected void displayContent(Page<UserResponse> page) {
        if (log.isInfoEnabled()) {
            page.content().forEach(user ->
                log.info("   ID: {} | {} | {} | {} | {}",
                        user.id(), user.name(), user.email(), user.age(),
                        userMapper.formatDateTime(user.createdAt())));
        }
    }
}