package com.crud.controller;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация контроллера пользователей.
 */
@Slf4j
public class UserControllerImpl implements UserController {

    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        log.debug("Контроллер: запрос на создание пользователя с email: {}", request.email());
        return userService.createUser(request);
    }

    @Override
    public UserResponse findUserById(Long id) {
        log.debug("Контроллер: поиск пользователя по id: {}", id);
        return userService.findUserById(id);
    }

    @Override
    public Page<UserResponse> findAllUsers(Pageable pageable) {
        log.debug("Контроллер: запрос пользователей с пагинацией: page={}, size={}", pageable.page(), pageable.size());
        return userService.findAllUsers(pageable);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        log.debug("Контроллер: обновление пользователя с id: {}", id);
        return userService.updateUser(id, request);
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Контроллер: удаление пользователя с id: {}", id);
        userService.deleteUser(id);
    }

    @Override
    public UserResponse findUserByEmail(String email) {
        return userService.findUserByEmail(email);
    }
}
