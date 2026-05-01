package com.crud.exception;

/**
 * Ошибка подключения к БД.
 */
public class DatabaseConnectionException extends UserServiceException {

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
