package com.crud.api.command.profile;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.api.command.PagedConsoleSupport;
import com.crud.controller.ProfileController;
import com.crud.dto.Page;
import com.crud.dto.ProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListProfilesCommandTest extends ConsoleCommandTest {

    @Mock
    private ProfileController profileController;

    @Mock
    private PagedConsoleSupport pagedConsoleSupport;

    @Test
    void execute_WhenProfilesExist_ShouldDisplayPage() {
        provideInput("0\n");
        ListProfilesCommand command = new ListProfilesCommand(profileController, getConsoleInput(), 5, pagedConsoleSupport);

        ProfileResponse profile = new ProfileResponse(1L, 1L, "+123", "Moscow", LocalDateTime.now(), LocalDateTime.now());
        Page<ProfileResponse> page = new Page<>(List.of(profile), 1, 0, 5);

        when(profileController.findAllProfiles(any())).thenReturn(page);
        when(pagedConsoleSupport.buildOptions(page)).thenReturn("Выберите действие [0-2]: ");

        command.execute();

        verify(profileController).findAllProfiles(any());
    }

    @Test
    void execute_WhenEmpty_ShouldShowEmptyMessage() {
        provideInput("");
        ListProfilesCommand command = new ListProfilesCommand(profileController, getConsoleInput());
        when(profileController.findAllProfiles(any())).thenReturn(new Page<>(List.of(), 0, 0, 5));

        command.execute();

        verify(profileController).findAllProfiles(any());
    }

    @Test
    void execute_WhenControllerThrows_ShouldNotThrow() {
        provideInput("");
        ListProfilesCommand command = new ListProfilesCommand(profileController, getConsoleInput());
        when(profileController.findAllProfiles(any())).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(command::execute);
    }
}
