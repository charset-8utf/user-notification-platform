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
        int result = ConsoleInput.readInt(scanner, "Введите число: ");
        assertEquals(42, result);
        assertTrue(outContent.toString().contains("Введите число: "));
    }

    @Test
    void readInt_shouldRetryOnInvalidInput() {
        String input = "abc\n42\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        int result = ConsoleInput.readInt(scanner, "Введите число: ");
        assertEquals(42, result);
        String output = outContent.toString();
        assertTrue(output.contains("❌ Ошибка: введите целое число."));
    }

    @Test
    void readLong_shouldReturnCorrectValue() {
        String input = "1234567890\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        long result = ConsoleInput.readLong(scanner, "Введите длинное число: ");
        assertEquals(1234567890L, result);
    }

    @Test
    void readLong_shouldRetryOnInvalidInput() {
        String input = "abc\n9876543210\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        long result = ConsoleInput.readLong(scanner, "Введите длинное число: ");
        assertEquals(9876543210L, result);
        String output = outContent.toString();
        assertTrue(output.contains("❌ Ошибка: введите число."));
    }

    @Test
    void readString_shouldReturnEnteredValue() {
        String input = "Привет\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        String result = ConsoleInput.readString(scanner, "Введите текст: ", "по умолчанию");
        assertEquals("Привет", result);
    }

    @Test
    void readString_shouldReturnDefaultWhenBlank() {
        String input = "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        String result = ConsoleInput.readString(scanner, "Введите текст: ", "по умолчанию");
        assertEquals("по умолчанию", result);
    }
}