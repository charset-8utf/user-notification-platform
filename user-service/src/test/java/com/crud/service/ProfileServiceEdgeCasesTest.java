package com.crud.service;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import com.crud.entity.User;
import com.crud.exception.ProfileNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.UserServiceException;
import com.crud.mapper.ProfileMapper;
import com.crud.repository.ProfileRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class ProfileServiceEdgeCasesTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void createProfile_WhenUserNotFound_ShouldThrowException() {
        ProfileRequest request = new ProfileRequest("+7999", "Address");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.createProfile(999L, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createProfile_WhenUserAlreadyHasProfile_ShouldThrowException() {
        ProfileRequest request = new ProfileRequest("+7999", "Address");
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> profileService.createProfile(1L, request))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void findProfileByUserId_WhenNotFound_ShouldThrowException() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.findProfileByUserId(999L))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateProfile_WhenNotFound_ShouldThrowException() {
        ProfileRequest request = new ProfileRequest("+7999", "New Address");
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(999L, request))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteProfile_WhenNotExists_ShouldThrowException() {
        when(profileRepository.existsByUserId(999L)).thenReturn(false);

        assertThatThrownBy(() -> profileService.deleteProfile(999L))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteProfile_WhenExists_ShouldDelegateToRepository() {
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        profileService.deleteProfile(1L);

        verify(profileRepository).deleteByUserId(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+7 (999) 123-45-67",
            "+79991234567",
            "89991234567",
            "123-45-67"
    })
    void createProfile_WithVariousPhoneFormats_ShouldWork(String phone) {
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(1L);
        ProfileRequest request = new ProfileRequest(phone, "Moscow");
        Profile profile = Profile.builder().phone(phone).address("Moscow").user(user).build();
        profile.setId(1L);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileMapper.toEntity(request)).thenReturn(Profile.builder().phone(phone).address("Moscow").build());
        when(profileRepository.save(any())).thenReturn(profile);
        when(profileMapper.toResponse(any())).thenReturn(
                new ProfileResponse(1L, 1L, phone, "Moscow", LocalDateTime.now(), LocalDateTime.now()));

        ProfileResponse result = profileService.createProfile(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.phone()).isEqualTo(phone);
    }
}
