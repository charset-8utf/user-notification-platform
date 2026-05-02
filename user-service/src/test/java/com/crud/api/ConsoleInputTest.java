package com.crud.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleInputTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void readInt_shouldReturnCorrectValue() {
        String input = "42\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        int result = consoleInput.readInt("Введите число: ");
        assertEquals(42, result);
        assertTrue(outContent.toString().contains("Введите число: "));
    }

    @Test
    void readInt_shouldRetryOnInvalidInput() {
        String input = "abc\n42\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        int result = consoleInput.readInt("Введите число: ");
        assertEquals(42, result);
        String output = outContent.toString();
        assertTrue(output.contains("Ошибка: введите целое число."));
    }

    @Test
    void readLong_shouldReturnCorrectValue() {
        String input = "1234567890\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        long result = consoleInput.readLong("Введите длинное число: ");
        assertEquals(1234567890L, result);
    }

    @Test
    void readLong_shouldRetryOnInvalidInput() {
        String input = "abc\n9876543210\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        long result = consoleInput.readLong("Введите длинное число: ");
        assertEquals(9876543210L, result);
        String output = outContent.toString();
        assertTrue(output.contains("Ошибка: введите число."));
    }

    @Test
    void readString_shouldReturnEnteredValue() {
        String input = "Привет\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        String result = consoleInput.readString("Введите текст: ", "по умолчанию");
        assertEquals("Привет", result);
    }

    @Test
    void readString_shouldReturnDefaultWhenBlank() {
        String input = "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        String result = consoleInput.readString("Введите текст: ", "по умолчанию");
        assertEquals("по умолчанию", result);
    }

    @Test
    void readIntWithDefault_WhenBlank_ShouldReturnDefault() {
        String input = "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        int result = consoleInput.readIntWithDefault("Введите возраст (Enter - оставить 18): ", 18);
        assertEquals(18, result);
    }

    @Test
    void readIntWithDefault_WhenNumber_ShouldReturnNumber() {
        String input = "25\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        int result = consoleInput.readIntWithDefault("Введите возраст: ", 18);
        assertEquals(25, result);
    }

    @Test
    void readIntWithDefault_WhenInvalidThenValid_ShouldRetry() {
        String input = "abc\n30\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        ConsoleInput consoleInput = new ConsoleInput(scanner);
        int result = consoleInput.readIntWithDefault("Введите возраст: ", 18);
        assertEquals(30, result);
        String output = outContent.toString();
        assertTrue(output.contains("Ошибка: введите целое число."));
    }
}
