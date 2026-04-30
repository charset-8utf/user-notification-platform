package com.crud.api.command.role;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.RoleController;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;

@Slf4j
public class UpdateRoleCommand implements Command {
    private final RoleController roleController;
    private final ConsoleInput consoleInput;

    public UpdateRoleCommand(RoleController roleController, ConsoleInput consoleInput) {
        this.roleController = roleController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID роли для обновления: ");
        try {
            RoleResponse existing = roleController.findRoleById(id);
            log.info("Текущее название: {}", existing.name());
            String newName = consoleInput.readString("Введите новое название (Enter - оставить без изменений): ", existing.name());
            RoleResponse updated = roleController.updateRole(id, new RoleRequest(newName));
            if (log.isInfoEnabled()) {
                log.info("Роль ID {} обновлена", updated.id());
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
