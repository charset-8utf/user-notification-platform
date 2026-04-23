package com.crud.exception;

/**
 * Исключение, возникающее при прерывании потока ввода из консоли.
 */
public class ConsoleInterruptedException extends UserServiceException {
    public ConsoleInterruptedException(String message) {
        super(message);
    }
}
