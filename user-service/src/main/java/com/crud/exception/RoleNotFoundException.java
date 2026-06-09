package com.crud.exception;

public class RoleNotFoundException extends UserServiceException {

    public RoleNotFoundException(Long id) {
        super("Роль с id " + id + " не найдена");
    }
}
