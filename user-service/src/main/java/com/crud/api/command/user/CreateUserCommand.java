package com.crud.api.command.user;

import lombok.extern.slf4j.Slf4j;

import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;

/**
 * Команда для создания нового пользователя.
 */
@Slf4j
public class CreateUserCommand implements Command {
    private final UserController controller;
    private final ConsoleInput consoleInput;

    public CreateUserCommand(UserController controller, ConsoleInput consoleInput) {
        this.controller = controller;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        String name = consoleInput.readString("Введите имя: ", "");
        String email = consoleInput.readString("Введите email: ", "");
        int age = consoleInput.readInt("Введите возраст: ");

        try {
            UserResponse response = controller.createUser(new UserRequest(name, email, age));
            log.info("Пользователь создан! ID: {}", response.id());
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}