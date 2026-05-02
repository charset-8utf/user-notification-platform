package com.crud.api.command.factory;

import com.crud.api.ConsoleInput;
import com.crud.api.MenuState;
import com.crud.api.command.Command;
import com.crud.api.command.role.AssignRoleToUserCommand;
import com.crud.api.command.role.CreateRoleCommand;
import com.crud.api.command.role.DeleteRoleCommand;
import com.crud.api.command.role.FindRoleByIdCommand;
import com.crud.api.command.role.ListRolesCommand;
import com.crud.api.command.role.RemoveRoleFromUserCommand;
import com.crud.api.command.role.UpdateRoleCommand;
import com.crud.controller.RoleController;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика команд управления ролями.
 */
public class RolesMenuCommandFactory implements MenuCommandFactory {
    private final RoleController roleController;
    private final ConsoleInput consoleInput;

    public RolesMenuCommandFactory(RoleController roleController, ConsoleInput consoleInput) {
        this.roleController = roleController;
        this.consoleInput = consoleInput;
    }

    @Override
    public MenuState state() {
        return MenuState.ROLES;
    }

    @Override
    public Map<Integer, Command> createCommands() {
        Map<Integer, Command> commands = new HashMap<>();
        commands.put(1, new CreateRoleCommand(roleController, consoleInput));
        commands.put(2, new FindRoleByIdCommand(roleController, consoleInput));
        commands.put(3, new ListRolesCommand(roleController, consoleInput));
        commands.put(4, new UpdateRoleCommand(roleController, consoleInput));
        commands.put(5, new DeleteRoleCommand(roleController, consoleInput));
        commands.put(6, new AssignRoleToUserCommand(roleController, consoleInput));
        commands.put(7, new RemoveRoleFromUserCommand(roleController, consoleInput));
        return commands;
    }
}
