package com.crud.api.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public abstract class ConsoleCommandTest {

    protected ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private Scanner scanner;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (scanner != null) {
            scanner.close();
        }
    }

    protected void provideInput(String data) {
        scanner = new Scanner(new ByteArrayInputStream(data.getBytes()));
    }

    protected Scanner getScanner() {
        return scanner;
    }

    protected String getOutput() {
        return outContent.toString();
    }
}