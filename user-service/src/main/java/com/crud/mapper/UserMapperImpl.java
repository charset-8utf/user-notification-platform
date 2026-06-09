package com.crud.mapper;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import org.springframework.stereotype.Component;

/**
 * Реализация маппера пользователей.
 */
@Component
public class UserMapperImpl implements UserMapper {

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
                user.getNotificationDeliveryStatus(),
                user.getCreatedAt()
        );
    }
}
