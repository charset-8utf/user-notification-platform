package com.crud.api.command.user;

import com.crud.api.command.ConsoleCommandTest;
import com.crud.controller.UserController;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersCommandTest extends ConsoleCommandTest {

    @Mock
    private UserController controller;

    @Test
    void execute_WhenUsersExist_ShouldPrintUsers() {
        provideInput("0\n");
        ListUsersCommand command = new ListUsersCommand(controller, getConsoleInput());

        UserResponse user1 = new UserResponse(1L, "User1", "u1@example.com", 20, LocalDateTime.now());
        UserResponse user2 = new UserResponse(2L, "User2", "u2@example.com", 25, LocalDateTime.now());
        Page<UserResponse> page = new Page<>(List.of(user1, user2), 2, 0, 5);

        when(controller.findAllUsers(argThat(p -> p.page() == 0))).thenReturn(page);

        command.execute();

        verify(controller).findAllUsers(any(Pageable.class));
    }

    @Test
    void execute_WhenNoUsers_ShouldReturnEmpty() {
        provideInput("0\n");
        ListUsersCommand command = new ListUsersCommand(controller, getConsoleInput());
        Page<UserResponse> emptyPage = new Page<>(List.of(), 0, 0, 5);

        when(controller.findAllUsers(argThat(p -> p.page() == 0))).thenReturn(emptyPage);

        command.execute();

        verify(controller).findAllUsers(any(Pageable.class));
    }
}
