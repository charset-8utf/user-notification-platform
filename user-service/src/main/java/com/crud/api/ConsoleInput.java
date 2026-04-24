package com.crud.api;

import com.crud.exception.ConsoleInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Утилитарный класс для безопасного чтения ввода из консоли.
 * <p>
 * Предоставляет методы для чтения целых чисел, длинных целых и строк
 * с автоматической обработкой ошибок ввода и повторным запросом.
 * Все методы используют цикл с флагом и логирование через SLF4J.
 * </p>
 */
public final class ConsoleInput {
    private static final Logger log = LoggerFactory.getLogger(ConsoleInput.class);

    private ConsoleInput() {
        throw new UnsupportedOperationException("Утилитарный класс, создание экземпляров запрещено");
    }

    /**
     * Читает целое число с консоли.
     * При некорректном вводе выводит сообщение об ошибке и повторяет запрос.
     *
     * @param scanner Scanner для чтения ввода
     * @param prompt  сообщение, выводимое перед вводом
     * @return введённое целое число
     */
    public static int readInt(Scanner scanner, String prompt) {
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
                log.error("❌ Ошибка: введите целое число.");
            }
        }
        return result;
    }

    /**
     * Читает целое число с консоли с возможностью пропуска (Enter).
     * Если введена пустая строка, возвращает значение по умолчанию.
     * При некорректном вводе выводит ошибку и повторяет запрос.
     *
     * @param scanner      Scanner для чтения ввода
     * @param prompt       сообщение, выводимое перед вводом
     * @param defaultValue значение по умолчанию при пустом вводе
     * @return введённое целое число или defaultValue
     */
    public static int readIntWithDefault(Scanner scanner, String prompt, int defaultValue) {
        log.info(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            log.error("❌ Ошибка: введите целое число.");
            return readIntWithDefault(scanner, prompt, defaultValue);
        }
    }

    /**
     * Читает длинное целое число с консоли.
     * При некорректном вводе выводит сообщение об ошибке и повторяет запрос.
     *
     * @param scanner Scanner для чтения ввода
     * @param prompt  сообщение, выводимое перед вводом
     * @return введённое длинное целое число
     */
    public static long readLong(Scanner scanner, String prompt) {
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
                log.error("❌ Ошибка: введите число.");
            }
        }
        return result;
    }

    /**
     * Читает строку с консоли. Если введена пустая строка, возвращает значение по умолчанию.
     *
     * @param scanner      Scanner для чтения ввода
     * @param prompt       сообщение, выводимое перед вводом
     * @param defaultValue значение, возвращаемое при пустом вводе
     * @return введённая строка или defaultValue
     */
    public static String readString(Scanner scanner, String prompt, String defaultValue) {
        log.info(prompt);
        String line = scanner.nextLine();
        return line.isBlank() ? defaultValue : line;
    }
}