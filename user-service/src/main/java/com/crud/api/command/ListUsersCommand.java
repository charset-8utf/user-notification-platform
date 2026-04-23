package com.crud.api.command;

import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import com.crud.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Команда для отображения списка всех пользователей.
 */
public class ListUsersCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(ListUsersCommand.class);
    private final UserController controller;

    public ListUsersCommand(UserController controller) {
        this.controller = controller;
    }

    @Override
    public void execute() {
        List<UserResponse> users = controller.findAllUsers();
        if (users.isEmpty()) {
            log.info("📭 Список пользователей пуст.");
        } else {
            log.info("👥 Список пользователей:");
            if (log.isInfoEnabled()) {
                for (UserResponse user : users) {
                    log.info("   ID: {} | {} | {} | {} | {}",
                            user.id(), user.name(), user.email(), user.age(),
                            UserMapper.formatDateTime(user.createdAt()));
                }
            }
        }
    }
}