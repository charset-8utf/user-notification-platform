package com.crud.api.command;

import lombok.extern.slf4j.Slf4j;

/**
 * Команда для завершения работы приложения.
 */
@Slf4j
public class ExitCommand implements Command {

    @Override
    public void execute() {
        log.info("Завершение работы...");
    }
}
