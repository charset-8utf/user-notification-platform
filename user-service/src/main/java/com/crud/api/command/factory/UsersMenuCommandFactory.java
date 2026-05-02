package com.crud.api.command.factory;

import com.crud.api.ConsoleInput;
import com.crud.api.MenuState;
import com.crud.api.command.Command;
import com.crud.api.command.user.CreateUserCommand;
import com.crud.api.command.user.DeleteUserCommand;
import com.crud.api.command.user.FindByEmailCommand;
import com.crud.api.command.user.FindUserCommand;
import com.crud.api.command.user.ListUsersCommand;
import com.crud.api.command.user.UpdateUserCommand;
import com.crud.controller.UserController;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика команд управления пользователями.
 */
public class UsersMenuCommandFactory implements MenuCommandFactory {
    private final UserController userController;
    private final ConsoleInput consoleInput;

    public UsersMenuCommandFactory(UserController userController, ConsoleInput consoleInput) {
        this.userController = userController;
        this.consoleInput = consoleInput;
    }

    @Override
    public MenuState state() {
        return MenuState.USERS;
    }

    @Override
    public Map<Integer, Command> createCommands() {
        Map<Integer, Command> commands = new HashMap<>();
        commands.put(1, new CreateUserCommand(userController, consoleInput));
        commands.put(2, new FindUserCommand(userController, consoleInput));
        commands.put(3, new FindByEmailCommand(userController, consoleInput));
        commands.put(4, new UpdateUserCommand(userController, consoleInput));
        commands.put(5, new DeleteUserCommand(userController, consoleInput));
        commands.put(6, new ListUsersCommand(userController, consoleInput));
        return commands;
    }
}
