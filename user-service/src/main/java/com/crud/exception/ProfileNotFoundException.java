package com.crud.exception;

/**
 * Профиль не найден.
 */
public class ProfileNotFoundException extends UserServiceException {

    public ProfileNotFoundException(Long userId) {
        super("Профиль для пользователя с id " + userId + " не найден");
    }

    public ProfileNotFoundException(String message) {
        super(message);
    }
}
