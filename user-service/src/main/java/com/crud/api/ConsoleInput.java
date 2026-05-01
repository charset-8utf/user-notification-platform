package com.crud.api;

import lombok.extern.slf4j.Slf4j;
import com.crud.exception.ConsoleInterruptedException;
import java.util.Scanner;

/**
 * Безопасное чтение ввода из консоли с валидацией.
 */
@Slf4j
public class ConsoleInput {
    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Читает целое число с повтором при ошибке.
     */
    public int readInt(String prompt) {
        boolean success = false;
        int result = 0;
        while (!success) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ConsoleInterruptedException("Ввод прерван");
            }
            log.info(prompt);
            if (scanner.hasNextInt()) {
                result = scanner.nextInt();
                scanner.nextLine();
                success = true;
            } else {
                scanner.next();
                log.error("Ошибка: введите целое число.");
            }
        }
        return result;
    }

    /**
     * Читает целое число или возвращает значение по умолчанию.
     */
    public int readIntWithDefault(String prompt, int defaultValue) {
        boolean valid = false;
        int result = defaultValue;
        while (!valid) {
            log.info(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return result;
            }
            try {
                result = Integer.parseInt(line);
                valid = true;
            } catch (NumberFormatException e) {
                log.error("Ошибка: введите целое число.");
            }
        }
        return result;
    }

    /**
     * Читает длинное целое число с повтором при ошибке.
     */
    public long readLong(String prompt) {
        boolean success = false;
        long result = 0;
        while (!success) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ConsoleInterruptedException("Ввод прерван");
            }
            log.info(prompt);
            if (scanner.hasNextLong()) {
                result = scanner.nextLong();
                scanner.nextLine();
                success = true;
            } else {
                scanner.next();
                log.error("Ошибка: введите число.");
            }
        }
        return result;
    }

    /**
     * Читает строку или возвращает значение по умолчанию.
     */
    public String readString(String prompt, String defaultValue) {
        log.info(prompt);
        String line = scanner.nextLine();
        return line.isBlank() ? defaultValue : line;
    }
}
