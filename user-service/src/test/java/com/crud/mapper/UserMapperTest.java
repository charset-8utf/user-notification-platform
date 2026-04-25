package com.crud.mapper;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toEntity_ShouldMapRequestToUser() {
        UserRequest request = new UserRequest("Иван", "ivan@example.com", 25);
        User user = UserMapper.toEntity(request);

        assertEquals("Иван", user.getName());
        assertEquals("ivan@example.com", user.getEmail());
        assertEquals(25, user.getAge());
        assertNull(user.getId());
    }

    @Test
    void toEntity_WithExistingUser_ShouldUpdateUser() {
        User existing = User.builder().name("Старое").email("old@example.com").age(20).build();
        existing.setId(1L);
        LocalDateTime existingDate = LocalDateTime.of(2025, 4, 24, 12, 0, 0);
        existing.setCreatedAt(existingDate);

        UserRequest request = new UserRequest("Новое", "new@example.com", 30);
        User updated = UserMapper.toEntity(request, existing);

        assertEquals(1L, updated.getId());
        assertEquals("Новое", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(30, updated.getAge());
        assertEquals(existingDate, updated.getCreatedAt()); // дата не изменилась
    }

    @Test
    void toResponse_ShouldMapUserToResponse() {
        User user = User.builder().name("Мария").email("maria@example.com").age(28).build();
        user.setId(10L);
        user.setCreatedAt(LocalDateTime.of(2025, 4, 24, 14, 30, 15));

        UserResponse response = UserMapper.toResponse(user);

        assertEquals(10L, response.id());
        assertEquals("Мария", response.name());
        assertEquals("maria@example.com", response.email());
        assertEquals(28, response.age());
        assertEquals(LocalDateTime.of(2025, 4, 24, 14, 30, 15), response.createdAt());
    }

    @Test
    void toResponseList_ShouldMapListOfUsers() {
        User user1 = User.builder().name("Анна").email("anna@example.com").age(22).build();
        user1.setId(1L);
        User user2 = User.builder().name("Петр").email("peter@example.com").age(35).build();
        user2.setId(2L);

        List<UserResponse> responses = UserMapper.toResponseList(List.of(user1, user2));

        assertEquals(2, responses.size());
        assertEquals("Анна", responses.get(0).name());
        assertEquals("peter@example.com", responses.get(1).email());
    }

    @Test
    void formatDateTime_ShouldReturnFormattedString() {
        LocalDateTime date = LocalDateTime.of(2025, 4, 24, 9, 5, 3);
        String formatted = UserMapper.formatDateTime(date);
        assertEquals("24.04.2025 09:05:03", formatted);
    }

    @Test
    void formatDateTime_WhenNull_ShouldReturnEmptyString() {
        assertEquals("", UserMapper.formatDateTime(null));
    }
}