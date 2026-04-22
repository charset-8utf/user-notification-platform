package com.crud.exception;

/**
 * Исключение, выбрасываемое при попытке создать пользователя с уже существующим email.
 * <p>
 * Возникает из-за нарушения уникальности поля email в базе данных.
 * </p>
 */
public class UserAlreadyExistsException extends UserServiceException {

    /**
     * Создаёт исключение для указанного email.
     *
     * @param email электронная почта, которая уже используется
     */
    public UserAlreadyExistsException(String email) {
        super("Пользователь с email " + email + " уже существует");
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message детальное описание ошибки
     * @param cause   исходная причина (например, ConstraintViolationException)
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}