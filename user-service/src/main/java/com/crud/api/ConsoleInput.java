package com.crud.api;

import java.util.Scanner;

/**
 * Утилитарный класс для безопасного чтения ввода из консоли.
 * <p>
 * Предоставляет методы для чтения целых чисел, длинных целых и строк
 * с автоматической обработкой ошибок ввода и повторным запросом.
 * Все методы рекурсивны.
 * </p>
 */
public final class ConsoleInput {

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
        System.out.print(prompt);
        if (scanner.hasNextInt()) {
            int val = scanner.nextInt();
            scanner.nextLine();
            return val;
        } else {
            scanner.next();
            System.out.println("❌ Ошибка: введите целое число.");
            return readInt(scanner, prompt);
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
        System.out.print(prompt);
        if (scanner.hasNextLong()) {
            long val = scanner.nextLong();
            scanner.nextLine();
            return val;
        } else {
            scanner.next();
            System.out.println("❌ Ошибка: введите число.");
            return readLong(scanner, prompt);
        }
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
        System.out.print(prompt);
        String line = scanner.nextLine();
        return line.isBlank() ? defaultValue : line;
    }
}