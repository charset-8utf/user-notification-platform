package com.crud.api.command;

import com.crud.api.MenuState;
import com.crud.api.command.factory.MenuCommandFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommandRegistryTest {

    @Test
    void get_ShouldReturnCommandForRegisteredStateAndChoice() {
        Command command = () -> { };
        MenuCommandFactory factory = new MenuCommandFactory() {
            @Override
            public MenuState state() {
                return MenuState.USERS;
            }

            @Override
            public Map<Integer, Command> createCommands() {
                return Map.of(1, command);
            }
        };

        CommandRegistry registry = new CommandRegistry(List.of(factory));

        assertNotNull(registry.get(MenuState.USERS, 1));
        assertNull(registry.get(MenuState.USERS, 2));
        assertNull(registry.get(MenuState.MAIN, 1));
    }
}
