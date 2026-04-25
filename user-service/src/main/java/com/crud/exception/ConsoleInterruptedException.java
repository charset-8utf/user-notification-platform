package com.crud.exception;

/**
 * Исключение, возникающее при прерывании потока ввода из консоли.
 */
public class ConsoleInterruptedException extends UserServiceException {
    public ConsoleInterruptedException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message детальное описание ошибки
     * @param cause   исходная причина
     */
    public ConsoleInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
