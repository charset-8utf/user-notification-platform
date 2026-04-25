package com.crud.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceExceptionTest {

    @Test
    void userNotFoundException_ShouldHaveCorrectMessage() {
        UserNotFoundException ex = new UserNotFoundException(99L);
        assertEquals("Пользователь с id 99 не найден", ex.getMessage());
    }

    @Test
    void userNotFoundException_WithCustomMessage() {
        UserNotFoundException ex = new UserNotFoundException("Свой текст ошибки");
        assertEquals("Свой текст ошибки", ex.getMessage());
    }

    @Test
    void userAlreadyExistsException_ShouldHaveCorrectMessage() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("test@example.com");
        assertEquals("Пользователь с email test@example.com уже существует", ex.getMessage());
    }

    @Test
    void validationException_ShouldStoreMessage() {
        ValidationException ex = new ValidationException("Некорректный возраст");
        assertEquals("Некорректный возраст", ex.getMessage());
    }

    @Test
    void dataAccessException_ShouldWrapCause() {
        RuntimeException cause = new RuntimeException("Connection failed");
        DataAccessException ex = new DataAccessException("Ошибка БД", cause);
        assertEquals("Ошибка БД", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void consoleInterruptedException_ShouldStoreMessage() {
        ConsoleInterruptedException ex = new ConsoleInterruptedException("Ввод прерван");
        assertEquals("Ввод прерван", ex.getMessage());
    }

    @Test
    void userNotFoundException_WithCause_ShouldStoreCause() {
        Exception cause = new RuntimeException("Ошибка базы данных");
        UserNotFoundException ex = new UserNotFoundException("Пользователь не найден", cause);
        assertEquals("Пользователь не найден", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void userAlreadyExistsException_WithCause_ShouldStoreCause() {
        Exception cause = new RuntimeException("Нарушение уникальности");
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Email уже существует", cause);
        assertEquals("Email уже существует", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void validationException_WithCause_ShouldStoreCause() {
        Exception cause = new IllegalArgumentException("Некорректный возраст");
        ValidationException ex = new ValidationException("Ошибка валидации", cause);
        assertEquals("Ошибка валидации", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void consoleInterruptedException_WithCause_ShouldStoreCause() {
        Exception cause = new InterruptedException();
        ConsoleInterruptedException ex = new ConsoleInterruptedException("Ввод прерван", cause);
        assertEquals("Ввод прерван", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void databaseConnectionException_ShouldStoreMessageAndCause() {
        Exception cause = new java.sql.SQLException("Отказ соединения");
        DatabaseConnectionException ex = new DatabaseConnectionException("Ошибка подключения к БД", cause);
        assertEquals("Ошибка подключения к БД", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}