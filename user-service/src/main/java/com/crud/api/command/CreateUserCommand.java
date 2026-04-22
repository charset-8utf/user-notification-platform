package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import java.util.Scanner;

/**
 * Команда для создания нового пользователя.
 */
public class CreateUserCommand implements Command {
    private final UserController controller;
    private final Scanner scanner;

    /**
     * Конструктор команды.
     *
     * @param controller контроллер пользователей
     * @param scanner    сканер для чтения ввода
     */
    public CreateUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    /**
     * Выполняет создание пользователя: запрашивает имя, email, возраст,
     * вызывает контроллер и выводит результат.
     *
     * @return {@code true}, чтобы продолжить работу приложения
     */
    @Override
    public boolean execute() {
        System.out.print("Введите имя: ");
        String name = scanner.nextLine();
        System.out.print("Введите email: ");
        String email = scanner.nextLine();
        int age = ConsoleInput.readInt(scanner, "Введите возраст: ");

        try {
            UserResponse response = controller.createUser(new UserRequest(name, email, age));
            System.out.printf("✅ Пользователь создан! ID: %d%n", response.id());
        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
        }
        return true;
    }
}