package com.crud.exception;

/**
 * Ошибка доступа к данным.
 */
public class DataAccessException extends UserServiceException {

  public DataAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
