package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Команда для удаления пользователя.
 */
public class DeleteUserCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(DeleteUserCommand.class);
    private final UserController controller;
    private final Scanner scanner;

    public DeleteUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        long id = ConsoleInput.readLong(scanner, "Введите ID пользователя для удаления: ");
        log.info("Вы уверены? (y/n): ");
        String confirm = scanner.nextLine();
        if (!"y".equalsIgnoreCase(confirm.trim())) {
            log.info("Удаление отменено.");
            return;
        }
        try {
            controller.deleteUser(id);
            log.info("✅ Пользователь с ID {} удалён", id);
        } catch (Exception e) {
            log.error("❌ Ошибка удаления: {}", e.getMessage(), e);
        }
    }
}