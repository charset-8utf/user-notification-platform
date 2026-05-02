package com.crud.exception;

/**
 * Пользователь уже существует.
 */
public class UserAlreadyExistsException extends UserServiceException {

    public UserAlreadyExistsException(String email) {
        super("Пользователь с email " + email + " уже существует");
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
