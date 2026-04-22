package com.crud.exception;

/**
 * Исключение, сигнализирующее о проблемах с подключением к базе данных.
 * <p>
 * Возникает при ошибках в конфигурации Hibernate, недоступности PostgreSQL,
 * неправильных учётных данных и т.д.
 * </p>
 * <p>
 * Обычно является фатальным для приложения, но может быть обработано для повторной попытки.
 * </p>
 */
public class DatabaseConnectionException extends UserServiceException {

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message детальное описание ошибки
     * @param cause   исходная причина (SQLException, HibernateException и т.п.)
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}