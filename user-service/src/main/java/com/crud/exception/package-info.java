/**
 * Иерархия кастомных исключений.
 * <p>
 * Базовое {@link com.crud.exception.UserServiceException} и его наследники:
 * {@link com.crud.exception.UserNotFoundException},
 * {@link com.crud.exception.UserAlreadyExistsException},
 * {@link com.crud.exception.DataAccessException},
 * {@link com.crud.exception.ValidationException},
 * {@link com.crud.exception.ConsoleInterruptedException}.
 * Обеспечивают понятную обработку ошибок.
 * </p>
 */
package com.crud.exception;