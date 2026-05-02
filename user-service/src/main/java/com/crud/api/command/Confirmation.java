package com.crud.api.command;

/**
 * Подтверждение действий пользователя.
 */
public enum Confirmation {
    YES("y"),
    NO("n");

    private final String value;

    Confirmation(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * Проверяет соответствие ввода текущему значению.
     */
    public boolean matches(String input) {
        if (input == null) {
            return false;
        }
        return this.value.equalsIgnoreCase(input.trim());
    }

    /**
     * Проверяет, является ли ввод подтверждением (yes).
     */
    public static boolean isConfirmed(String input) {
        return YES.matches(input);
    }
}
