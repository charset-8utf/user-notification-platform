package com.crud.service;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import com.crud.exception.UserNotFoundException;
import com.crud.mapper.UserMapper;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldSaveAndReturnResponse() {
        UserRequest request = new UserRequest("John", "john@example.com", 30);
        User user = User.builder().name("John").email("john@example.com").age(30).build();
        User savedUser = User.builder().name("John").email("john@example.com").age(30).build();
        savedUser.setId(1L);
        savedUser.setCreatedAt(LocalDateTime.now());
        UserResponse expectedResponse = new UserResponse(1L, "John", "john@example.com", 30, savedUser.getCreatedAt());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse actual = userService.createUser(request);

        assertThat(actual).isEqualTo(expectedResponse);
        verify(userRepository).save(user);
        verify(userMapper).toEntity(request);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    void findUserById_WhenExists_ShouldReturnResponse() {
        Long userId = 1L;
        User user = User.builder().name("Jane").email("jane@example.com").age(25).build();
        user.setId(userId);
        user.setCreatedAt(LocalDateTime.now());
        UserResponse expected = new UserResponse(userId, "Jane", "jane@example.com", 25, user.getCreatedAt());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse actual = userService.findUserById(userId);

        assertThat(actual).isEqualTo(expected);
        verify(userRepository).findById(userId);
    }

    @Test
    void findUserById_WhenNotExists_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(String.valueOf(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void findAllUsers_ShouldReturnPageOfResponses() {
        User user = User.builder().name("John").email("john@example.com").age(30).build();
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());
        UserResponse response = new UserResponse(1L, "John", "john@example.com", 30, user.getCreatedAt());
        Page<User> userPage = new PageImpl<>(List.of(user));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(userRepository.findAll(pageRequest)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(response);

        Page<UserResponse> result = userService.findAllUsers(pageRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(response);
        verify(userRepository).findAll(pageRequest);
    }

    @Test
    void deleteUser_WhenExists_ShouldDelete() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WhenNotExists_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findUserByEmail_WhenExists_ShouldReturnResponse() {
        String email = "john@example.com";
        User user = User.builder().name("John").email(email).age(30).build();
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());
        UserResponse expected = new UserResponse(1L, "John", email, 30, user.getCreatedAt());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse actual = userService.findUserByEmail(email);

        assertThat(actual).isEqualTo(expected);
        verify(userRepository).findByEmail(email);
    }
}
