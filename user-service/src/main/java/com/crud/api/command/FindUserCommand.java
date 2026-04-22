package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import com.crud.dto.UserResponse;
import java.util.Scanner;

/**
 * Команда для поиска пользователя по ID.
 */
public class FindUserCommand implements Command {
    private final UserController controller;
    private final Scanner scanner;

    /**
     * Конструктор команды.
     *
     * @param controller контроллер пользователей
     * @param scanner    сканер для чтения ввода
     */
    public FindUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    /**
     * Запрашивает ID, вызывает контроллер и выводит информацию о пользователе.
     *
     * @return {@code true} для продолжения работы
     */
    @Override
    public boolean execute() {
        long id = ConsoleInput.readLong(scanner, "Введите ID пользователя: ");
        try {
            UserResponse user = controller.findUserById(id);
            System.out.printf("""
                    🔍 Найден пользователь:
                       ID: %d
                       Имя: %s
                       Email: %s
                       Возраст: %d
                       Создан: %s
                    """, user.id(), user.name(), user.email(), user.age(), user.createdAt());
        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
        }
        return true;
    }
}