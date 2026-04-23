package com.crud.api.command;

/**
 * Интерфейс команды для консольного меню (паттерн Command).
 * Каждая операция меню реализует этот интерфейс.
 */
@FunctionalInterface
public interface Command {
    /**
     * Выполняет команду.
     */
    void execute();
}