package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.controller.UserController;
import java.util.Scanner;

/**
 * Команда для удаления пользователя по ID с запросом подтверждения.
 */
public class DeleteUserCommand implements Command {
    private final UserController controller;
    private final Scanner scanner;

    /**
     * Конструктор команды.
     *
     * @param controller контроллер пользователей
     * @param scanner    сканер для чтения ввода
     */
    public DeleteUserCommand(UserController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    /**
     * Запрашивает ID и подтверждение, затем вызывает удаление через контроллер.
     *
     * @return {@code true} всегда (приложение продолжает работу)
     */
    @Override
    public boolean execute() {
        long id = ConsoleInput.readLong(scanner, "Введите ID пользователя для удаления: ");
        System.out.print("Вы уверены? (y/n): ");
        String confirm = scanner.nextLine();
        if (!"y".equalsIgnoreCase(confirm.trim())) {
            System.out.println("Удаление отменено.");
            return true;
        }
        try {
            controller.deleteUser(id);
            System.out.printf("✅ Пользователь с ID %d удалён%n", id);
        } catch (Exception e) {
            System.err.println("❌ Ошибка удаления: " + e.getMessage());
        }
        return true;
    }
}