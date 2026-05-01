package com.crud.api.command;

/**
 * Интерфейс команды (паттерн Command).
 */
@FunctionalInterface
public interface Command {
    void execute();
}
