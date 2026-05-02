package com.crud.exception;

/**
 * Пользователь не найден.
 */
public class UserNotFoundException extends UserServiceException {

  public UserNotFoundException(Long id) {
    super("Пользователь с id " + id + " не найден");
  }

  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
