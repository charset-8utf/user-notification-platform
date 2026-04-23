package com.crud.api.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Команда для завершения работы приложения.
 */
public class ExitCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(ExitCommand.class);

    @Override
    public void execute() {
        log.info("Завершение работы...");
    }
}