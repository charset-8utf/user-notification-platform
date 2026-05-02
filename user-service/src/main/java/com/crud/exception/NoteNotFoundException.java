package com.crud.exception;

/**
 * Заметка не найдена.
 */
public class NoteNotFoundException extends UserServiceException {

    public NoteNotFoundException(Long id) {
        super("Заметка с id " + id + " не найдена");
    }

    public NoteNotFoundException(String message) {
        super(message);
    }
}
