package com.crud.controller;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;

/**
 * Контроллер пользователей.
 */
public interface UserController {

    /**
     * Создаёт пользователя.
     */
    UserResponse createUser(UserRequest request);

    /**
     * Находит пользователя по ID.
     */
    UserResponse findUserById(Long id);

    /**
     * Возвращает страницу пользователей.
     */
    Page<UserResponse> findAllUsers(Pageable pageable);

    /**
     * Обновляет пользователя.
     */
    UserResponse updateUser(Long id, UserRequest request);

    /**
     * Удаляет пользователя.
     */
    void deleteUser(Long id);

    /**
     * Находит пользователя по email.
     */
    UserResponse findUserByEmail(String email);
}
