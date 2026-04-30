package com.crud.api.command.profile;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.ProfileController;
import com.crud.dto.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProfileCommandTest extends ConsoleCommandTest {

    @Mock
    private ProfileController profileController;

    @Test
    void execute_WhenProfileExists_ShouldCallController() {
        provideInput("1\n");
        GetProfileCommand command = new GetProfileCommand(profileController, getConsoleInput());
        when(profileController.findProfileByUserId(1L))
                .thenReturn(new ProfileResponse(1L, 1L, "+1234567890", "Moscow", LocalDateTime.now(), LocalDateTime.now()));

        command.execute();

        verify(profileController).findProfileByUserId(1L);
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("999\n");
        GetProfileCommand command = new GetProfileCommand(profileController, getConsoleInput());
        when(profileController.findProfileByUserId(999L)).thenThrow(new RuntimeException("Profile not found"));

        assertDoesNotThrow(command::execute);
    }
}
