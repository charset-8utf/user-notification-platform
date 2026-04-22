package com.crud.exception;

/**
 * Исключение, возникающее при ошибках доступа к данным, не связанных с подключением.
 * <p>
 * Оборачивает низкоуровневые исключения Hibernate и JDBC (например, ошибки запросов,
 * проблемы с транзакциями, нарушения ограничений, кроме уникальности email).
 * </p>
 */
public class DataAccessException extends UserServiceException {

  /**
   * Создаёт исключение с сообщением и причиной.
   *
   * @param message детальное описание ошибки
   * @param cause   исходное исключение (например, HibernateException)
   */
  public DataAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}