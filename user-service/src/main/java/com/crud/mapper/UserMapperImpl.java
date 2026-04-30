package com.crud.mapper;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Реализация маппера пользователей.
 */
public class UserMapperImpl implements UserMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public User toEntity(UserRequest request) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
    }

    @Override
    public User toEntity(UserRequest request, User existing) {
        existing.setName(request.name());
        existing.setEmail(request.email());
        existing.setAge(request.age());
        return existing;
    }

    @Override
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }

    @Override
    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
    }
}
