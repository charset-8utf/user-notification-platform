package com.crud.util;

import com.crud.entity.NotificationDeliveryStatus;

import com.crud.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDateTime;

import static com.crud.util.CustomAssert.*;

@Execution(ExecutionMode.CONCURRENT)
class CustomAssertTest {

    @Test
    void userResponseAssert_ShouldWork() {
        UserResponse user = new UserResponse(1L, "John Doe", "john@test.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());

        assertThatUser(user)
                .hasId(1L)
                .hasName("John Doe")
                .hasEmail("john@test.com")
                .hasAge(30);
    }

    @Test
    void userResponseAssert_Match_ShouldWork() {
        UserResponse expected = new UserResponse(1L, "John", "john@test.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());
        UserResponse actual = new UserResponse(1L, "John", "john@test.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());

        assertThatUser(actual).matches(expected);
    }

    @Test
    void profileResponseAssert_ShouldWork() {
        ProfileResponse profile = new ProfileResponse(1L, 2L, "+79991112233", "Moscow", LocalDateTime.now(), LocalDateTime.now());

        assertThatProfile(profile)
                .hasUserId(2L)
                .hasPhone("+79991112233")
                .hasAddress("Moscow");
    }

    @Test
    void noteResponseAssert_ShouldWork() {
        NoteResponse note = new NoteResponse(1L, "Test content", LocalDateTime.now(), LocalDateTime.now());

        assertThatNote(note)
                .hasId(1L)
                .hasContent("Test content");
    }

    @Test
    void roleResponseAssert_ShouldWork() {
        RoleResponse role = new RoleResponse(1L, "ADMIN", LocalDateTime.now(), LocalDateTime.now());

        assertThatRole(role)
                .hasId(1L)
                .hasName("ADMIN");
    }
}
