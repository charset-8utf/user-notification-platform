package com.crud.exception;

public class NoteNotFoundException extends UserServiceException {

    public NoteNotFoundException(Long id) {
        super("Заметка с id " + id + " не найдена");
    }
}
