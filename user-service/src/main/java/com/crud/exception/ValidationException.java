package com.crud.exception;

/**
 * Ошибка валидации.
 */
public class ValidationException extends UserServiceException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
