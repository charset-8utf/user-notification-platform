package com.crud.api.command.factory;

import com.crud.api.MenuState;
import com.crud.api.command.Command;

import java.util.Map;

/**
 * Фабрика команд для меню.
 */
public interface MenuCommandFactory {
    MenuState state();

    Map<Integer, Command> createCommands();
}
