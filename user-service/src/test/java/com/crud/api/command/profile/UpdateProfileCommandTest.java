package com.crud.api.command.profile;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.ProfileController;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProfileCommandTest extends ConsoleCommandTest {

    @Mock
    private ProfileController profileController;

    @Test
    void execute_ShouldUpdateProfile() {
        provideInput("1\n+9998887776\nNew York\n");
        UpdateProfileCommand command = new UpdateProfileCommand(profileController, getConsoleInput());
        when(profileController.findProfileByUserId(1L))
                .thenReturn(new ProfileResponse(1L, 1L, "+123", "Old City", LocalDateTime.now(), LocalDateTime.now()));
        when(profileController.updateProfile(eq(1L), any(ProfileRequest.class)))
                .thenReturn(new ProfileResponse(1L, 1L, "+9998887776", "New York", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(profileController).updateProfile(eq(1L), any(ProfileRequest.class));
    }

    @Test
    void execute_WhenBlankInput_ShouldKeepExisting() {
        provideInput("1\n\n\n");
        UpdateProfileCommand command = new UpdateProfileCommand(profileController, getConsoleInput());
        when(profileController.findProfileByUserId(1L))
                .thenReturn(new ProfileResponse(1L, 1L, "ExistingPhone", "ExistingAddress", LocalDateTime.now(), LocalDateTime.now()));
        when(profileController.updateProfile(eq(1L), any(ProfileRequest.class)))
                .thenReturn(new ProfileResponse(1L, 1L, "ExistingPhone", "ExistingAddress", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(profileController).updateProfile(eq(1L), argThat(req ->
            req.phone().equals("ExistingPhone") && req.address().equals("ExistingAddress")
        ));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("999\n");
        UpdateProfileCommand command = new UpdateProfileCommand(profileController, getConsoleInput());
        when(profileController.findProfileByUserId(999L)).thenThrow(new RuntimeException("Profile not found"));

        assertDoesNotThrow(command::execute);
    }
}
