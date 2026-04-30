package com.crud.api.command.role;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.api.command.Confirmation;
import com.crud.controller.RoleController;

@Slf4j
public class DeleteRoleCommand implements Command {
    private final RoleController roleController;
    private final ConsoleInput consoleInput;

    public DeleteRoleCommand(RoleController roleController, ConsoleInput consoleInput) {
        this.roleController = roleController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID роли для удаления: ");
        String confirm = consoleInput.readString("Вы уверены? (y/n): ", "n");
        if (!Confirmation.isConfirmed(confirm)) {
            log.info("Удаление отменено.");
            return;
        }
        try {
            roleController.deleteRole(id);
            log.info("Роль с ID {} удалена", id);
        } catch (RuntimeException e) {
            log.error("Ошибка удаления: {}", e.getMessage(), e);
        }
    }
}
