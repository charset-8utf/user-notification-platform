package com.crud.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileNotFoundExceptionTest {

    @Test
    void constructor_WithUserId_ShouldCreateExceptionWithMessage() {
        ProfileNotFoundException exception = new ProfileNotFoundException(1L);

        assertEquals("Профиль для пользователя с id 1 не найден", exception.getMessage());
    }

    @Test
    void constructor_WithMessage_ShouldCreateException() {
        ProfileNotFoundException exception = new ProfileNotFoundException("Профиль не существует");

        assertEquals("Профиль не существует", exception.getMessage());
    }

    @Test
    void shouldBeUserServiceException() {
        ProfileNotFoundException exception = new ProfileNotFoundException(1L);

        assertInstanceOf(UserServiceException.class, exception);
    }
}
