package com.crud.api.command;

import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import com.crud.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class FindByEmailCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(FindByEmailCommand.class);
    private final UserController controller;
    private final Scanner scanner;

    public FindByEmailCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        log.info("Введите email пользователя для поиска: ");
        String email = scanner.nextLine().trim();
        try {
            UserResponse user = controller.findUserByEmail(email);
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