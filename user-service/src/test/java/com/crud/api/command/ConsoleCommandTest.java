package com.crud.api.command;

import com.crud.api.ConsoleInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public abstract class ConsoleCommandTest {

    protected ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private ConsoleInput consoleInput;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    protected void provideInput(String data) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(data.getBytes()));
        consoleInput = new ConsoleInput(scanner);
    }

    protected ConsoleInput getConsoleInput() {
        return consoleInput;
    }
}
