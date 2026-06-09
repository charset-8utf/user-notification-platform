package com.crud.mapper;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;

/**
 * Маппер пользователей.
 */
public interface UserMapper {

    User toEntity(UserRequest request);
    User toEntity(UserRequest request, User existing);
    UserResponse toResponse(User user);
}
