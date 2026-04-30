package com.crud.api.command.role;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.RoleController;
import com.crud.dto.RoleResponse;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

@Slf4j
public class FindRoleByIdCommand implements Command {
    private final RoleController roleController;
    private final ConsoleInput consoleInput;
    private final UserMapper userMapper;

    public FindRoleByIdCommand(RoleController roleController, ConsoleInput consoleInput) {
        this.roleController = roleController;
        this.consoleInput = consoleInput;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    public void execute() {
        long id = consoleInput.readLong("Введите ID роли: ");
        try {
            RoleResponse role = roleController.findRoleById(id);
            if (log.isInfoEnabled()) {
                log.info("""
                        Найдена роль:
                           ID: {}
                           Название: {}
                           Создана: {}
                           Обновлена: {}
                        """, role.id(), role.name(),
                        userMapper.formatDateTime(role.createdAt()),
                        userMapper.formatDateTime(role.updatedAt()));
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
