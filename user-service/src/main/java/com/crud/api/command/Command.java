package com.crud.api.command;

/**
 * Интерфейс команды для консольного меню (паттерн Command).
 * Каждая операция меню реализует этот интерфейс.
 */
@FunctionalInterface
public interface Command {

    /**
     * Выполняет команду.
     *
     * @return {@code true}, если приложение должно продолжить работу;
     *         {@code false}, если требуется завершение (например, команда выхода)
     */
    boolean execute();
}