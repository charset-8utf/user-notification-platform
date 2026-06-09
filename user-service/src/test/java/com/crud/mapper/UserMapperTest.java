package com.crud.mapper;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.NotificationDeliveryStatus;
import com.crud.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Execution(ExecutionMode.CONCURRENT)
class UserMapperTest {

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void toEntity_FromRequest_ShouldMapAllFields() {
        UserRequest request = new UserRequest("John Doe", "john@example.com", 30);

        User entity = userMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("John Doe");
        assertThat(entity.getEmail()).isEqualTo("john@example.com");
        assertThat(entity.getAge()).isEqualTo(30);
    }

    @Test
    void toResponse_FromEntity_ShouldMapAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        User entity = User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .age(25)
                .build();
        entity.setId(1L);
        entity.setCreatedAt(createdAt);
        entity.setNotificationDeliveryStatus(NotificationDeliveryStatus.FAILED);

        UserResponse response = userMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane@example.com");
        assertThat(response.age()).isEqualTo(25);
        assertThat(response.notificationDeliveryStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void toEntity_WithExistingEntity_ShouldUpdateFields() {
        User existing = User.builder()
                .name("Old Name")
                .email("old@example.com")
                .age(20)
                .build();
        existing.setId(1L);
        existing.setCreatedAt(LocalDateTime.now());

        UserRequest request = new UserRequest("New Name", "new@example.com", 35);

        User result = userMapper.toEntity(request, existing);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getAge()).isEqualTo(35);
    }

    @Test
    void toEntity_FromRequest_NullRequest_ShouldThrowNullPointerException() {
        assertThatThrownBy(() -> userMapper.toEntity(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toResponse_FromEntity_NullEntity_ShouldThrowNullPointerException() {
        assertThatThrownBy(() -> userMapper.toResponse(null))
                .isInstanceOf(NullPointerException.class);
    }
}
