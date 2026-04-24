package com.crud.api.command;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ExitCommandTest {

    @Test
    void execute_ShouldPrintGoodbye() {
        var out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        ExitCommand command = new ExitCommand();
        command.execute();
        System.setOut(System.out);

        assertTrue(out.toString().contains("Завершение работы..."));
    }
}