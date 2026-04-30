package com.crud.mapper;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Маппер пользователей.
 */
public interface UserMapper {

    User toEntity(UserRequest request);
    User toEntity(UserRequest request, User existing);
    UserResponse toResponse(User user);
    List<UserResponse> toResponseList(List<User> users);
    String formatDateTime(LocalDateTime dateTime);
}
