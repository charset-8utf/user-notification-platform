package com.crud.api.command.profile;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.ProfileController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProfileCommandTest extends ConsoleCommandTest {

    @Mock
    private ProfileController profileController;

    @Test
    void execute_WhenConfirmed_ShouldDeleteProfile() {
        provideInput("1\ny\n");
        DeleteProfileCommand command = new DeleteProfileCommand(profileController, getConsoleInput());
        doNothing().when(profileController).deleteProfile(1L);

        command.execute();

        verify(profileController).deleteProfile(1L);
    }

    @Test
    void execute_WhenNotConfirmed_ShouldNotDelete() {
        provideInput("1\nn\n");
        DeleteProfileCommand command = new DeleteProfileCommand(profileController, getConsoleInput());

        command.execute();

        verify(profileController, never()).deleteProfile(anyLong());
    }

    @Test
    void execute_WhenControllerThrowsException_ShouldNotThrow() {
        provideInput("1\ny\n");
        DeleteProfileCommand command = new DeleteProfileCommand(profileController, getConsoleInput());
        doThrow(new RuntimeException("Profile not found")).when(profileController).deleteProfile(1L);

        assertDoesNotThrow(command::execute);
    }
}
