package com.crud.mapper;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class RoleMapperTest {

    private final RoleMapper roleMapper = new RoleMapperImpl();

    @Test
    void toEntity_FromRequest_ShouldMapName() {
        RoleRequest request = new RoleRequest("ADMIN");

        Role entity = roleMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("ADMIN");
    }

    @Test
    void toResponse_FromEntity_ShouldMapAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        Role role = Role.builder().name("USER").build();
        role.setId(1L);
        role.setCreatedAt(createdAt);
        role.setUpdatedAt(updatedAt);

        RoleResponse response = roleMapper.toResponse(role);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("USER");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toEntity_WithExistingEntity_ShouldUpdateName() {
        Role existing = Role.builder().name("OLD_ROLE").build();
        existing.setId(1L);
        RoleRequest request = new RoleRequest("NEW_ROLE");

        Role result = roleMapper.toEntity(request, existing);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("NEW_ROLE");
    }
}
