package com.crud.exception;

public class ProfileNotFoundException extends UserServiceException {

    public ProfileNotFoundException(Long userId) {
        super("Профиль для пользователя с id " + userId + " не найден");
    }
}
