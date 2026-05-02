package com.crud.api.command.role;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.RoleController;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;

@Slf4j
public class CreateRoleCommand implements Command {
    private final RoleController roleController;
    private final ConsoleInput consoleInput;

    public CreateRoleCommand(RoleController roleController, ConsoleInput consoleInput) {
        this.roleController = roleController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID роли (например, 1=ADMIN, 2=USER): ");
        if (id <= 0) {
            log.error("ID роли должен быть положительным числом.");
            return;
        }
        String name = consoleInput.readString("Введите название новой роли: ", "");
        if (name.isBlank()) {
            log.error("Название роли не может быть пустым.");
            return;
        }
        try {
            RoleResponse role = roleController.createRole(new RoleRequest(id, name));
            if (log.isInfoEnabled()) {
                log.info("Создана роль: ID={}, Название={}", role.id(), role.name());
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
