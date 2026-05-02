package com.crud.api.command.factory;

import com.crud.api.MenuRouter;
import com.crud.api.MenuState;
import com.crud.api.command.Command;
import com.crud.api.command.NavigateMenuCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика команд главного меню.
 */
public class MainMenuCommandFactory implements MenuCommandFactory {
    private final MenuRouter menuRouter;

    public MainMenuCommandFactory(MenuRouter menuRouter) {
        this.menuRouter = menuRouter;
    }

    @Override
    public MenuState state() {
        return MenuState.MAIN;
    }

    @Override
    public Map<Integer, Command> createCommands() {
        Map<Integer, Command> commands = new HashMap<>();
        commands.put(1, new NavigateMenuCommand(menuRouter, MenuState.USERS));
        commands.put(2, new NavigateMenuCommand(menuRouter, MenuState.NOTES));
        commands.put(3, new NavigateMenuCommand(menuRouter, MenuState.ROLES));
        commands.put(4, new NavigateMenuCommand(menuRouter, MenuState.PROFILES));
        return commands;
    }
}
