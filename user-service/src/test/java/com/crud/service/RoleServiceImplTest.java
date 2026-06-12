package com.crud.service;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import com.crud.entity.User;
import com.crud.exception.RoleNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.mapper.RoleMapper;
import com.crud.repository.RoleRepository;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class RoleServiceImplTest {

    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2026, 6, 1, 12, 0);

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRole_ShouldSaveAndReturnResponse() {
        RoleRequest request = new RoleRequest("ADMIN");
        Role role = Role.builder().name("ADMIN").build();
        Role savedRole = Role.builder().name("ADMIN").build();
        savedRole.setId(1L);
        RoleResponse expected = new RoleResponse(1L, "ADMIN", TEST_TIME, TEST_TIME);

        when(roleMapper.toEntity(request)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(savedRole);
        when(roleMapper.toResponse(savedRole)).thenReturn(expected);

        RoleResponse actual = roleService.createRole(request);

        assertThat(actual).isEqualTo(expected);
        verify(roleRepository).save(role);
    }

    @Test
    void findRoleById_WhenExists_ShouldReturnResponse() {
        Long roleId = 1L;
        Role role = Role.builder().name("USER").build();
        role.setId(roleId);
        RoleResponse expected = new RoleResponse(roleId, "USER", TEST_TIME, TEST_TIME);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(expected);

        RoleResponse actual = roleService.findRoleById(roleId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findRoleById_WhenNotExists_ShouldThrowRoleNotFoundException() {
        Long roleId = 999L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findRoleById(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining(String.valueOf(roleId));
    }

    @Test
    void findAllRoles_ShouldReturnPage() {
        Role role = Role.builder().name("ADMIN").build();
        role.setId(1L);
        RoleResponse response = new RoleResponse(1L, "ADMIN", TEST_TIME, TEST_TIME);
        Page<Role> rolePage = new PageImpl<>(List.of(role));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(roleRepository.findAll(pageRequest)).thenReturn(rolePage);
        when(roleMapper.toResponse(role)).thenReturn(response);

        Page<RoleResponse> result = roleService.findAllRoles(pageRequest);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void deleteRole_ShouldDelete() {
        Long roleId = 1L;
        when(roleRepository.existsById(roleId)).thenReturn(true);
        doNothing().when(roleRepository).deleteById(roleId);

        roleService.deleteRole(roleId);

        verify(roleRepository).existsById(roleId);
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    void deleteRole_WhenNotExists_ShouldThrowRoleNotFoundException() {
        Long roleId = 999L;
        when(roleRepository.existsById(roleId)).thenReturn(false);

        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining(String.valueOf(roleId));
    }

    @Test
    void assignRoleToUser_WhenUserNotExists_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        Long roleId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignRoleToUser(userId, roleId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(String.valueOf(userId));
    }

    @Test
    void assignRoleToUser_WhenRoleNotExists_ShouldThrowRoleNotFoundException() {
        Long userId = 1L;
        Long roleId = 999L;
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(userId);
        user.setRoles(new HashSet<>());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignRoleToUser(userId, roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining(String.valueOf(roleId));
    }

    @Test
    void assignRoleToUser_ShouldAddRoleToUser() {
        Long userId = 1L;
        Long roleId = 2L;
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(userId);
        user.setRoles(new HashSet<>());
        Role role = Role.builder().name("ADMIN").build();
        role.setId(roleId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        roleService.assignRoleToUser(userId, roleId);

        assertThat(user.getRoles()).contains(role);
    }

    @Test
    void removeRoleFromUser_ShouldRemoveRoleFromUser() {
        Long userId = 1L;
        Long roleId = 2L;
        Role role = Role.builder().name("ADMIN").build();
        role.setId(roleId);
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(userId);
        user.setRoles(new HashSet<>());
        user.getRoles().add(role);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        roleService.removeRoleFromUser(userId, roleId);

        assertThat(user.getRoles()).doesNotContain(role);
    }
}
