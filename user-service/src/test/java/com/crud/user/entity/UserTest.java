package com.crud.user.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testUserConstructorAndGetters() {
        User user = new User("John Doe", "john@example.com", 30);
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(30, user.getAge());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testUserBuilder() {
        User user = User.builder()
                .name("Jane")
                .email("jane@example.com")
                .age(25)
                .build();
        assertEquals("Jane", user.getName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals(25, user.getAge());
    }
}