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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void createProfile_ShouldSaveAndReturnResponse() {
        Long userId = 1L;
        ProfileRequest request = new ProfileRequest("+1234567890", "Test Address");
        User user = User.builder().name("John").build();
        user.setId(userId);
        
        Profile savedProfile = Profile.builder().phone("+1234567890").address("Test Address").user(user).build();
        savedProfile.setId(1L);
        savedProfile.setUser(user);
        
        ProfileResponse expected = new ProfileResponse(1L, userId, "+1234567890", "Test Address", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(userId)).thenReturn(false);
        when(profileMapper.toEntity(request)).thenReturn(Profile.builder().phone("+1234567890").address("Test Address").build());
        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);
        when(profileMapper.toResponse(savedProfile)).thenReturn(expected);

        ProfileResponse actual = profileService.createProfile(userId, request);

        assertThat(actual).isEqualTo(expected);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void createProfile_WhenProfileExists_ShouldThrowUserServiceException() {
        Long userId = 1L;
        ProfileRequest request = new ProfileRequest("+1234567890", "Address");
        User user = User.builder().name("John").build();
        user.setId(userId);
        
        Profile existingProfile = Profile.builder().phone("+999").user(user).build();
        existingProfile.setId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> profileService.createProfile(userId, request))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void createProfile_WhenUserNotExists_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        ProfileRequest request = new ProfileRequest("+1234567890", "Address");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.createProfile(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(String.valueOf(userId));
    }

    @Test
    void findProfileByUserId_WhenExists_ShouldReturnResponse() {
        Long userId = 1L;
        User user = User.builder().name("John").build();
        user.setId(userId);
        
        Profile profile = Profile.builder().phone("+1234567890").address("Test Address").user(user).build();
        profile.setId(1L);
        
        ProfileResponse expected = new ProfileResponse(1L, userId, "+1234567890", "Test Address", null, null);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(expected);

        ProfileResponse actual = profileService.findProfileByUserId(userId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findProfileByUserId_WhenNotExists_ShouldThrowProfileNotFoundException() {
        Long userId = 999L;
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.findProfileByUserId(userId))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining(String.valueOf(userId));
    }

    @Test
    void findAllProfiles_ShouldReturnPage() {
        Long userId = 1L;
        User user = User.builder().name("John").build();
        user.setId(userId);
        
        Profile profile = Profile.builder().phone("+1234567890").user(user).build();
        profile.setId(1L);
        
        ProfileResponse response = new ProfileResponse(1L, userId, "+1234567890", null, null, null);
        Page<Profile> profilePage = new PageImpl<>(List.of(profile));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(profileRepository.findAll(pageRequest)).thenReturn(profilePage);
        when(profileMapper.toResponse(profile)).thenReturn(response);

        Page<ProfileResponse> result = profileService.findAllProfiles(pageRequest);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void deleteProfile_ShouldDelete() {
        Long userId = 1L;
        when(profileRepository.existsByUserId(userId)).thenReturn(true);
        doNothing().when(profileRepository).deleteByUserId(userId);

        profileService.deleteProfile(userId);

        verify(profileRepository).deleteByUserId(userId);
    }
}
