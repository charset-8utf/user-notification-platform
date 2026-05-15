package com.crud.service;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.UserMapper;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class UserServiceEdgeCasesTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @ParameterizedTest
    @ValueSource(strings = {"existing@email.com", "duplicate@test.com", "taken@mail.ru"})
    void createUser_WhenEmailAlreadyExists_ShouldThrowValidationException(String email) {
        UserRequest requestWithExistingEmail = new UserRequest("John", email, 30);
        User user = User.builder().name("John").email(email).age(30).build();
        when(userMapper.toEntity(requestWithExistingEmail)).thenReturn(user);
        when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> userService.createUser(requestWithExistingEmail))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(email);
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        UserRequest updateRequest = new UserRequest("John", "test@example.com", 30);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findUserById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "verylongemailaddressthatexceedslimit@example.com"})
    void findUserByEmail_WithVariousEmails_ShouldWork(String email) {
        User user = User.builder()
                .name("Test")
                .email(email)
                .age(25)
                .build();
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse(1L, "Test", email, 25, LocalDateTime.now()));

        UserResponse result = userService.findUserByEmail(email);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(email);
    }
}
