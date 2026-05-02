package com.crud.api.command.role;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.api.command.Confirmation;
import com.crud.controller.RoleController;

@Slf4j
public class RemoveRoleFromUserCommand implements Command {
    private final RoleController roleController;
    private final ConsoleInput consoleInput;

    public RemoveRoleFromUserCommand(RoleController roleController, ConsoleInput consoleInput) {
        this.roleController = roleController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long userId = consoleInput.readLong("Введите ID пользователя: ");
        long roleId = consoleInput.readLong("Введите ID роли: ");
        String confirm = consoleInput.readString("Вы уверены? (y/n): ", "n");
        if (!Confirmation.isConfirmed(confirm)) {
            log.info("Отмена.");
            return;
        }
        try {
            roleController.removeRoleFromUser(userId, roleId);
            log.info("Роль ID {} снята с пользователя ID {}", roleId, userId);
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
