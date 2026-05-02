package com.crud.api.command.user;

import lombok.extern.slf4j.Slf4j;

import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.api.command.Confirmation;
import com.crud.controller.UserController;

/**
 * Команда для удаления пользователя.
 */
@Slf4j
public class DeleteUserCommand implements Command {
    private final UserController controller;
    private final ConsoleInput consoleInput;

    public DeleteUserCommand(UserController controller, ConsoleInput consoleInput) {
        this.controller = controller;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID пользователя для удаления: ");
        String confirm = consoleInput.readString("Вы уверены? (y/n): ", "n");
        if (!Confirmation.isConfirmed(confirm)) {
            log.info("Удаление отменено.");
            return;
        }
        try {
            controller.deleteUser(id);
            log.info("Пользователь с ID {} удалён", id);
        } catch (RuntimeException e) {
            log.error("Ошибка удаления: {}", e.getMessage(), e);
        }
    }
}