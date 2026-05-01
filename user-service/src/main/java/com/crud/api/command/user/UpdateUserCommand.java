package com.crud.api.command.user;

import lombok.extern.slf4j.Slf4j;

import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;

/**
 * Команда для обновления данных существующего пользователя.
 */
@Slf4j
public class UpdateUserCommand implements Command {
    private final UserController controller;
    private final ConsoleInput consoleInput;

    public UpdateUserCommand(UserController controller, ConsoleInput consoleInput) {
        this.controller = controller;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID пользователя для обновления: ");
        UserResponse existing;
        try {
            existing = controller.findUserById(id);
        } catch (RuntimeException e) {
            log.error("Пользователь не найден: {}", e.getMessage(), e);
            return;
        }

        String name = consoleInput.readString("Новое имя (Enter - оставить \"" + existing.name() + "\"): ", existing.name());
        String email = consoleInput.readString("Новый email (Enter - оставить \"" + existing.email() + "\"): ", existing.email());
        int age = consoleInput.readIntWithDefault("Новый возраст (Enter - оставить " + existing.age() + "): ", existing.age());

        try {
            UserResponse updated = controller.updateUser(id, new UserRequest(name, email, age));
            log.info("Пользователь с ID {} обновлён", updated.id());
        } catch (RuntimeException e) {
            log.error("Ошибка обновления: {}", e.getMessage(), e);
        }
    }
}