package com.crud.api.command;


import com.crud.api.MenuState;
import com.crud.api.command.factory.MenuCommandFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реестр команд для каждого состояния меню.
 */
public class CommandRegistry {
    private final Map<MenuState, Map<Integer, Command>> commandsByState = new EnumMap<>(MenuState.class);

    public CommandRegistry(List<MenuCommandFactory> factories) {
        commandsByState.putAll(factories.stream()
                .collect(Collectors.toMap(
                        MenuCommandFactory::state,
                        MenuCommandFactory::createCommands,
                        (left, right) -> right,
                        () -> new EnumMap<>(MenuState.class)
                )));
    }

    public Command get(MenuState state, int choice) {
        Map<Integer, Command> commands = commandsByState.get(state);
        if (commands == null) {
            return null;
        }
        return commands.get(choice);
    }
}
