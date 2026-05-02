package com.crud.mapper;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import com.crud.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileMapperTest {

    private final ProfileMapper mapper = new ProfileMapperImpl();

    @Test
    void toEntity_ShouldMapRequestToProfile() {
        ProfileRequest request = new ProfileRequest("+1234567890", "Moscow");

        Profile result = mapper.toEntity(request);

        assertEquals("+1234567890", result.getPhone());
        assertEquals("Moscow", result.getAddress());
    }

    @Test
    void toResponse_ShouldMapProfileToResponse() {
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Profile profile = Profile.builder().phone("+1234567890").address("Moscow").user(user).build();
        profile.setId(1L);
        profile.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        profile.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 12, 0));

        ProfileResponse result = mapper.toResponse(profile);

        assertEquals(1L, result.id());
        assertEquals("+1234567890", result.phone());
        assertEquals("Moscow", result.address());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
    }

    @Test
    void toEntity_WithExisting_ShouldUpdateProfile() {
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Profile existing = Profile.builder().phone("+111").address("Old").user(user).build();
        existing.setId(1L);

        ProfileRequest request = new ProfileRequest("+999", "New");
        Profile result = mapper.toEntity(request, existing);

        assertEquals("+999", result.getPhone());
        assertEquals("New", result.getAddress());
        assertSame(existing, result);
    }

    @Test
    void toResponseList_ShouldMapListOfProfiles() {
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Profile profile1 = Profile.builder().phone("+111").address("City1").user(user).build();
        profile1.setId(1L);
        Profile profile2 = Profile.builder().phone("+222").address("City2").user(user).build();
        profile2.setId(2L);

        List<ProfileResponse> result = mapper.toResponseList(List.of(profile1, profile2));

        assertEquals(2, result.size());
        assertEquals("+111", result.get(0).phone());
        assertEquals("+222", result.get(1).phone());
    }

    @Test
    void toResponseList_WithNull_ShouldReturnEmptyList() {
        List<ProfileResponse> result = mapper.toResponseList(null);

        assertTrue(result.isEmpty());
    }
}
