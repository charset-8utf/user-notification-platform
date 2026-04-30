package com.crud.api.command.profile;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.ProfileController;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProfileCommandTest extends ConsoleCommandTest {

    @Mock
    private ProfileController profileController;

    @Test
    void execute_ShouldCreateProfile() {
        provideInput("1\n+1234567890\nMoscow\n");
        CreateProfileCommand command = new CreateProfileCommand(profileController, getConsoleInput());
        when(profileController.createProfile(eq(1L), any(ProfileRequest.class)))
                .thenReturn(new ProfileResponse(1L, 1L, "+1234567890", "Moscow", null, null));

        command.execute();

        verify(profileController).createProfile(eq(1L), any(ProfileRequest.class));
    }

    @Test
    void execute_WithEmptyOptionalFields_ShouldCreateProfile() {
        provideInput("1\n\n\n");
        CreateProfileCommand command = new CreateProfileCommand(profileController, getConsoleInput());
        when(profileController.createProfile(eq(1L), any(ProfileRequest.class)))
                .thenReturn(new ProfileResponse(1L, 1L, "", "", null, null));

        command.execute();

        verify(profileController).createProfile(eq(1L), any(ProfileRequest.class));
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\n+123\nCity\n");
        CreateProfileCommand command = new CreateProfileCommand(profileController, getConsoleInput());
        when(profileController.createProfile(eq(1L), any(ProfileRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        assertDoesNotThrow(command::execute);
    }
}
