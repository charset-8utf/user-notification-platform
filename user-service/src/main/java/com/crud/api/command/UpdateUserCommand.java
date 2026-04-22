package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import java.util.Scanner;

/**
 * Команда для обновления данных существующего пользователя.
 */
public class UpdateUserCommand implements Command {
    private final UserController controller;
    private final Scanner scanner;

    /**
     * Конструктор команды.
     *
     * @param controller контроллер пользователей
     * @param scanner    сканер для чтения ввода
     */
    public UpdateUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    /**
     * Запрашивает ID, загружает текущие данные, позволяет изменить поля,
     * затем вызывает обновление через контроллер.
     *
     * @return {@code true} для продолжения работы
     */
    @Override
    public boolean execute() {
        long id = ConsoleInput.readLong(scanner, "Введите ID пользователя для обновления: ");
        UserResponse existing;
        try {
            existing = controller.findUserById(id);
        } catch (Exception e) {
            System.err.println("❌ Пользователь не найден: " + e.getMessage());
            return true;
        }

        String name = ConsoleInput.readString(scanner,
                "Новое имя (Enter - оставить \"" + existing.name() + "\"): ", existing.name());
        String email = ConsoleInput.readString(scanner,
                "Новый email (Enter - оставить \"" + existing.email() + "\"): ", existing.email());
        int age = ConsoleInput.readInt(scanner,
                "Новый возраст (Enter - оставить " + existing.age() + "): ");
        if (age == 0 && existing.age() != 0) {
            age = existing.age();
        }

        try {
            UserResponse updated = controller.updateUser(id, new UserRequest(name, email, age));
            System.out.printf("✅ Пользователь с ID %d обновлён%n", updated.id());
        } catch (Exception e) {
            System.err.println("❌ Ошибка обновления: " + e.getMessage());
        }
        return true;
    }
}