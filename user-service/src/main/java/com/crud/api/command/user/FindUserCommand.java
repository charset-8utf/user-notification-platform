package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import com.crud.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Команда для поиска пользователя по ID.
 */
public class FindUserCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(FindUserCommand.class);
    private final UserController controller;
    private final Scanner scanner;

    public FindUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        long id = ConsoleInput.readLong(scanner, "Введите ID пользователя: ");
        try {
            UserResponse user = controller.findUserById(id);
            if (log.isInfoEnabled()) {
                log.info("""
                        🔍 Найден пользователь:
                           ID: {}
                           Имя: {}
                           Email: {}
                           Возраст: {}
                           Создан: {}
                        """, user.id(), user.name(), user.email(), user.age(),
                        UserMapper.formatDateTime(user.createdAt()));
            }
        } catch (Exception e) {
            log.error("❌ Ошибка: {}", e.getMessage(), e);
        }
    }
}