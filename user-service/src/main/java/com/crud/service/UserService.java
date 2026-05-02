package com.crud.service;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;

/**
 * Сервис пользователей.
 */
public interface UserService {

    UserResponse createUser(UserRequest request);
    UserResponse findUserById(Long id);
    Page<UserResponse> findAllUsers(Pageable pageable);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    UserResponse findUserByEmail(String email);
}
