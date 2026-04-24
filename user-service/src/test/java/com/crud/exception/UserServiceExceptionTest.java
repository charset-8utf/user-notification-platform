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
}