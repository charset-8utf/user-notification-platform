package com.crud.exception;

/**
 * Исключение, выбрасываемое при нарушении правил валидации входных данных.
 * <p>
 * Может использоваться вместо {@link IllegalArgumentException} для единообразия.
 * </p>
 */
public class ValidationException extends UserServiceException {

    /**
     * Создаёт исключение с сообщением о нарушении валидации.
     *
     * @param message детальное описание ошибки
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message детальное описание ошибки
     * @param cause   исходная причина
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}