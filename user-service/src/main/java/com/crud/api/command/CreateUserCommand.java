package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Команда для создания нового пользователя.
 */
public class CreateUserCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(CreateUserCommand.class);
    private final UserController controller;
    private final Scanner scanner;

    public CreateUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        log.info("Введите имя: ");
        String name = scanner.nextLine();
        log.info("Введите email: ");
        String email = scanner.nextLine();
        int age = ConsoleInput.readInt(scanner, "Введите возраст: ");

        try {
            UserResponse response = controller.createUser(new UserRequest(name, email, age));
            log.info("✅ Пользователь создан! ID: {}", response.id());
        } catch (Exception e) {
            log.error("❌ Ошибка: {}", e.getMessage(), e);
        }
    }
}