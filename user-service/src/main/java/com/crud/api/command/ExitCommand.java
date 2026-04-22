package com.crud.api.command;

/**
 * Команда для завершения работы приложения.
 */
public class ExitCommand implements Command {

    /**
     * Выводит сообщение о завершении и возвращает {@code false},
     * что приводит к остановке основного цикла.
     *
     * @return {@code false} – сигнал остановить приложение
     */
    @Override
    public boolean execute() {
        System.out.println("Завершение работы...");
        return false;
    }
}