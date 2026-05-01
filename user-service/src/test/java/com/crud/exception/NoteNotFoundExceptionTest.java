package com.crud.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteNotFoundExceptionTest {

    @Test
    void constructor_WithId_ShouldCreateExceptionWithMessage() {
        NoteNotFoundException exception = new NoteNotFoundException(1L);

        assertEquals("Заметка с id 1 не найдена", exception.getMessage());
    }

    @Test
    void constructor_WithMessage_ShouldCreateException() {
        NoteNotFoundException exception = new NoteNotFoundException("Заметка не существует");

        assertEquals("Заметка не существует", exception.getMessage());
    }

    @Test
    void shouldBeUserServiceException() {
        NoteNotFoundException exception = new NoteNotFoundException(1L);

        assertInstanceOf(UserServiceException.class, exception);
    }
}
