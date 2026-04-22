package com.crud.api.command;

import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import java.util.List;

/**
 * Команда для отображения списка всех пользователей.
 */
public class ListUsersCommand implements Command {
    private final UserController controller;

    /**
     * Конструктор команды.
     *
     * @param controller контроллер пользователей
     */
    public ListUsersCommand(UserController controller) {
        this.controller = controller;
    }

    /**
     * Получает список всех пользователей через контроллер и выводит его в консоль.
     *
     * @return {@code true} для продолжения работы
     */
    @Override
    public boolean execute() {
        List<UserResponse> users = controller.findAllUsers();
        if (users.isEmpty()) {
            System.out.println("📭 Список пользователей пуст.");
        } else {
            System.out.println("👥 Список пользователей:");
            users.forEach(user -> System.out.printf(
                    "   ID: %d | %s | %s | %d | %s%n",
                    user.id(), user.name(), user.email(), user.age(), user.createdAt()
            ));
        }
        return true;
    }
}