package com.crud.controller;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Реализация {@link UserController}.
 * <p>
 * Просто делегирует все вызовы сервису {@link UserService}.
 * Не содержит никакой дополнительной логики, так как вся бизнес-логика уже в сервисе.
 * </p>
 */
public class UserControllerImpl implements UserController {

    private static final Logger log = LoggerFactory.getLogger(UserControllerImpl.class);
    private final UserService userService;

    /**
     * Конструктор с внедрением зависимости сервиса.
     *
     * @param userService сервис для работы с пользователями
     */
    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse createUser(UserRequest request) {
        log.debug("Контроллер: запрос на создание пользователя с email: {}", request.email());
        return userService.createUser(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse findUserById(Long id) {
        log.debug("Контроллер: поиск пользователя по id: {}", id);
        return userService.getUserById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserResponse> findAllUsers() {
        log.debug("Контроллер: запрос всех пользователей");
        return userService.getAllUsers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        log.debug("Контроллер: обновление пользователя с id: {}", id);
        return userService.updateUser(id, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(Long id) {
        log.debug("Контроллер: удаление пользователя с id: {}", id);
        userService.deleteUser(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse findUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }
}