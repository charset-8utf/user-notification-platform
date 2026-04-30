package com.crud.exception;

/**
 * Роль не найдена.
 */
public class RoleNotFoundException extends UserServiceException {

    public RoleNotFoundException(Long id) {
        super("Роль с id " + id + " не найдена");
    }

    public RoleNotFoundException(String message) {
        super(message);
    }
}
