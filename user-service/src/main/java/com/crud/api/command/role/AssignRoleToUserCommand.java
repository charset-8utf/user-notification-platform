package com.crud.api.command.role;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.RoleController;

@Slf4j
public class AssignRoleToUserCommand implements Command {
    private final RoleController roleController;
    private final ConsoleInput consoleInput;

    public AssignRoleToUserCommand(RoleController roleController, ConsoleInput consoleInput) {
        this.roleController = roleController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long userId = consoleInput.readLong("Введите ID пользователя: ");
        long roleId = consoleInput.readLong("Введите ID роли: ");
        try {
            roleController.assignRoleToUser(userId, roleId);
            log.info("Роль ID {} назначена пользователю ID {}", roleId, userId);
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
