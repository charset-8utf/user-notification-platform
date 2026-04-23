package com.crud.api;

import com.crud.exception.ConsoleInterruptedException;

import java.util.Scanner;

public final class ConsoleInput {

    private ConsoleInput() {
        throw new UnsupportedOperationException("Утилитарный класс");
    }

    public static int readInt(Scanner scanner, String prompt) {
        boolean success = false;
        int result = 0;
        while (!success) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ConsoleInterruptedException("Ввод прерван");
            }
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                result = scanner.nextInt();
                scanner.nextLine();
                success = true;
            } else {
                scanner.next();
                System.out.println("❌ Ошибка: введите целое число.");
            }
        }
        return result;
    }

    public static long readLong(Scanner scanner, String prompt) {
        boolean success = false;
        long result = 0;
        while (!success) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ConsoleInterruptedException("Ввод прерван");
            }
            System.out.print(prompt);
            if (scanner.hasNextLong()) {
                result = scanner.nextLong();
                scanner.nextLine();
                success = true;
            } else {
                scanner.next();
                System.out.println("❌ Ошибка: введите число.");
            }
        }
        return result;
    }

    public static String readString(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        return line.isBlank() ? defaultValue : line;
    }
}