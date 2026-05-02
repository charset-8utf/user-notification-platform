package com.crud.api.command.user;

import lombok.extern.slf4j.Slf4j;

import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

@Slf4j
public class FindByEmailCommand implements Command {
    private final UserController controller;
    private final ConsoleInput consoleInput;
    private final UserMapper userMapper;

    public FindByEmailCommand(UserController controller, ConsoleInput consoleInput) {
        this.controller = controller;
        this.consoleInput = consoleInput;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    public void execute() {
        String email = consoleInput.readString("Введите email пользователя для поиска: ", "");
        try {
            UserResponse user = controller.findUserByEmail(email);
            if (log.isInfoEnabled()) {
                log.info("""
                    Найден пользователь:
                       ID: {}
                       Имя: {}
                       Email: {}
                       Возраст: {}
                       Создан: {}
                    """, user.id(), user.name(), user.email(), user.age(),
                        userMapper.formatDateTime(user.createdAt()));
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}