package com.crud.service;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import com.crud.exception.ProfileNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.repository.ProfileRepository;
import com.crud.repository.UserRepository;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createProfile_ShouldCreateAndReturnResponse() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        Profile profile = Profile.builder().phone("+1234567890").address("Moscow").user(user).build();
        profile.setId(1L);
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        ProfileResponse result = service.createProfile(1L, new ProfileRequest("+1234567890", "Moscow"));

        assertEquals(1L, result.id());
        assertEquals("+1234567890", result.phone());
        assertEquals("Moscow", result.address());
    }

    @Test
    void createProfile_WhenUserNotFound_ShouldThrow() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ProfileRequest request = new ProfileRequest("+123", "City");
        assertThrows(UserNotFoundException.class, () -> service.createProfile(1L, request));
    }

    @Test
    void createProfile_WhenProfileExists_ShouldThrow() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Profile existing = Profile.builder().phone("+111").address("Old").user(user).build();
        existing.setId(1L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        ProfileRequest request = new ProfileRequest("+123", "City");
        assertThrows(DataAccessException.class, () -> service.createProfile(1L, request));
    }

    @Test
    void findProfileByUserId_WhenExists_ShouldReturnResponse() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Profile profile = Profile.builder().phone("+123").address("City").user(user).build();
        profile.setId(1L);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        ProfileResponse result = service.findProfileByUserId(1L);

        assertEquals(1L, result.id());
        assertEquals("+123", result.phone());
    }

    @Test
    void findProfileByUserId_WhenNotFound_ShouldThrow() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> service.findProfileByUserId(1L));
    }

    @Test
    void updateProfile_ShouldUpdateAndReturnResponse() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Profile profile = Profile.builder().phone("+123").address("City").user(user).build();
        profile.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.update(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse result = service.updateProfile(1L, new ProfileRequest("+999", "NewCity"));

        assertEquals("+999", result.phone());
        assertEquals("NewCity", result.address());
    }

    @Test
    void deleteProfile_ShouldCallRepositoryDelete() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        doNothing().when(profileRepository).deleteByUserId(1L);

        service.deleteProfile(1L);

        verify(profileRepository).deleteByUserId(1L);
    }

    @Test
    void updateProfile_WithOptimisticLockException_ShouldRetryAndSucceed() {
        ProfileServiceImpl service = new ProfileServiceImpl(profileRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Profile profile = Profile.builder().phone("+123").address("City").user(user).build();
        profile.setId(1L);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user))
                .thenReturn(Optional.of(user))
                .thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile))
                .thenReturn(Optional.of(profile))
                .thenReturn(Optional.of(profile));
        when(profileRepository.update(any(Profile.class)))
                .thenThrow(new DataAccessException("Optimistic lock", new StaleObjectStateException("Profile", 1L)))
                .thenThrow(new DataAccessException("Optimistic lock", new StaleObjectStateException("Profile", 1L)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse result = service.updateProfile(1L, new ProfileRequest("+999", "New"));

        assertNotNull(result);
        verify(profileRepository, times(3)).update(any(Profile.class));
    }
}
