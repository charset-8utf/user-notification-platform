package com.crud.exception;

/**
 * Базовое исключение для всех ошибок, связанных с бизнес-логикой приложения.
 * <p>
 * Все специфические исключения наследуются от этого класса.
 * Это позволяет перехватывать все ошибки приложения одним блоком catch,
 * не теряя детализации.
 * </p>
 * <p>
 * Исключение является непроверяемым (unchecked), так как в большинстве случаев
 * восстановление невозможно или требует вмешательства пользователя.
 * </p>
 */
public class UserServiceException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message детальное описание ошибки
     */
    public UserServiceException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message детальное описание ошибки
     * @param cause   исходная причина (например, исключение Hibernate или SQL)
     */
    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}