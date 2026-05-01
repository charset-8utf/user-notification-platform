package com.crud.exception;

/**
 * Прерывание консольного ввода.
 */
public class ConsoleInterruptedException extends UserServiceException {

    public ConsoleInterruptedException(String message) {
        super(message);
    }

    public ConsoleInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
