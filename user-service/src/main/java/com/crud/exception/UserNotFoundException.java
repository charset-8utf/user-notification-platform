package com.crud.exception;

/**
 * Исключение, выбрасываемое при попытке найти пользователя с несуществующим идентификатором.
 * <p>
 * Используется в сервисе и репозитории при вызовах findById, update, delete.
 * </p>
 */
public class UserNotFoundException extends UserServiceException {

  /**
   * Создаёт исключение для указанного идентификатора.
   *
   * @param id идентификатор пользователя, который не был найден
   */
  public UserNotFoundException(Long id) {
    super("Пользователь с id " + id + " не найден");
  }

  /**
   * Создаёт исключение с произвольным сообщением (для особых случаев).
   *
   * @param message детальное описание ошибки
   */
  public UserNotFoundException(String message) {
    super(message);
  }

  /**
   * Создаёт исключение с сообщением и причиной.
   *
   * @param message детальное описание ошибки
   * @param cause   исходная причина
   */
  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}