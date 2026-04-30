package com.crud.controller;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerImplTest {

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileControllerImpl profileController;

    @Test
    void createProfile_ShouldDelegateToService() {
        ProfileRequest request = new ProfileRequest("+1234567890", "Moscow");
        ProfileResponse expected = new ProfileResponse(1L, 1L, "+1234567890", "Moscow", LocalDateTime.now(), LocalDateTime.now());
        when(profileService.createProfile(1L, request)).thenReturn(expected);

        ProfileResponse actual = profileController.createProfile(1L, request);

        assertEquals(expected, actual);
        verify(profileService).createProfile(1L, request);
    }

    @Test
    void findProfileByUserId_ShouldDelegateToService() {
        ProfileResponse expected = new ProfileResponse(1L, 1L, "+1234567890", "Moscow", LocalDateTime.now(), LocalDateTime.now());
        when(profileService.findProfileByUserId(1L)).thenReturn(expected);

        ProfileResponse actual = profileController.findProfileByUserId(1L);

        assertEquals(expected, actual);
        verify(profileService).findProfileByUserId(1L);
    }

    @Test
    void updateProfile_ShouldDelegateToService() {
        ProfileRequest request = new ProfileRequest("+9998887776", "New York");
        ProfileResponse expected = new ProfileResponse(1L, 1L, "+9998887776", "New York", LocalDateTime.now(), LocalDateTime.now());
        when(profileService.updateProfile(1L, request)).thenReturn(expected);

        ProfileResponse actual = profileController.updateProfile(1L, request);

        assertEquals(expected, actual);
        verify(profileService).updateProfile(1L, request);
    }

    @Test
    void deleteProfile_ShouldDelegateToService() {
        doNothing().when(profileService).deleteProfile(1L);

        profileController.deleteProfile(1L);

        verify(profileService).deleteProfile(1L);
    }
}
