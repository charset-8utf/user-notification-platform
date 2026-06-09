package com.crud.mapper;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import com.crud.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Execution(ExecutionMode.CONCURRENT)
class ProfileMapperTest {

    private final ProfileMapper profileMapper = new ProfileMapperImpl();

    @Test
    void toResponse_FromEntity_ShouldMapAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(1L);

        Profile profile = Profile.builder().phone("+1234567890").address("Test Address").user(user).build();
        profile.setId(10L);
        profile.setCreatedAt(createdAt);
        profile.setUpdatedAt(updatedAt);

        ProfileResponse response = profileMapper.toResponse(profile);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.phone()).isEqualTo("+1234567890");
        assertThat(response.address()).isEqualTo("Test Address");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toResponse_WhenUserIsNull_ShouldMapUserIdAsNull() {
        Profile profile = Profile.builder().phone("+000").address("Addr").build();
        profile.setId(1L);

        ProfileResponse response = profileMapper.toResponse(profile);

        assertThat(response.userId()).isNull();
    }

    @Test
    void toEntity_WithExistingEntity_ShouldUpdateFields() {
        Profile existing = Profile.builder().phone("+000").address("Old Address").build();
        existing.setId(1L);
        ProfileRequest request = new ProfileRequest("+999", "New Address");

        Profile result = profileMapper.toEntity(request, existing);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPhone()).isEqualTo("+999");
        assertThat(result.getAddress()).isEqualTo("New Address");
    }

    @Test
    void toEntity_NullRequest_ShouldThrowNullPointerException() {
        Profile existing = Profile.builder().phone("+000").address("Old").build();
        assertThatThrownBy(() -> profileMapper.toEntity(null, existing))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toResponse_NullEntity_ShouldThrowNullPointerException() {
        assertThatThrownBy(() -> profileMapper.toResponse(null))
                .isInstanceOf(NullPointerException.class);
    }
}
