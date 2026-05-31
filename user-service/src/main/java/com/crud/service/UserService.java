package com.crud.service;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(UserRequest request);
    UserResponse findUserById(Long id);
    Page<UserResponse> findAllUsers(Pageable pageable);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    UserResponse findUserByEmail(String email);
    UserResponse findUserByUsername(String username);
    Page<UserResponse> searchUsersByEmail(String emailPart, Pageable pageable);
}
