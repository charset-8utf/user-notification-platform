package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Команда для обновления данных существующего пользователя.
 */
public class UpdateUserCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(UpdateUserCommand.class);
    private final UserController controller;
    private final Scanner scanner;

    public UpdateUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        long id = ConsoleInput.readLong(scanner, "Введите ID пользователя для обновления: ");
        UserResponse existing;
        try {
            existing = controller.findUserById(id);
        } catch (Exception e) {
            log.error("❌ Пользователь не найден: {}", e.getMessage(), e);
            return;
        }

        String name = ConsoleInput.readString(scanner, "Новое имя (Enter - оставить \"" + existing.name() + "\"): ", existing.name());
        String email = ConsoleInput.readString(scanner, "Новый email (Enter - оставить \"" + existing.email() + "\"): ", existing.email());
        int age = ConsoleInput.readInt(scanner, "Новый возраст (Enter - оставить " + existing.age() + "): ");
        if (age == 0 && existing.age() != 0) age = existing.age();

        try {
            UserResponse updated = controller.updateUser(id, new UserRequest(name, email, age));
            log.info("✅ Пользователь с ID {} обновлён", updated.id());
        } catch (Exception e) {
            log.error("❌ Ошибка обновления: {}", e.getMessage(), e);
        }
    }
}