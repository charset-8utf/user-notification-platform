package com.crud.api.command.user;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.api.command.ExitCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExitCommandTest extends ConsoleCommandTest {

    @Test
    void execute_ShouldNotThrow() {
        ExitCommand command = new ExitCommand();

        assertDoesNotThrow(command::execute);
    }
}
