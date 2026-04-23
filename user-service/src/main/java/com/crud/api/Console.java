package com.crud.api;

import com.crud.api.command.*;
import com.crud.controller.UserController;
import java.util.Map;
import java.util.Scanner;

public class Console {
    private final Map<Integer, Command> commands;
    private final Scanner scanner;

    public Console(UserController controller) {
        this.scanner = new Scanner(System.in);
        this.commands = Map.of(
                1, new CreateUserCommand(controller, scanner),
                2, new FindUserCommand(controller, scanner),
                3, new UpdateUserCommand(controller, scanner),
                4, new DeleteUserCommand(controller, scanner),
                5, new ListUsersCommand(controller),
                0, new ExitCommand()
        );
    }

    public void start() {
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = ConsoleInput.readInt(scanner, "Ваш выбор: ");
                Command cmd = commands.get(choice);
                if (cmd == null) {
                    System.out.println("❌ Неверный выбор. Введите число от 0 до 5.");
                    continue;
                }
                cmd.execute();
                if (cmd instanceof ExitCommand) {
                    running = false;
                }
            }
        } finally {
            scanner.close();
        }
    }

    private void printMenu() {
        System.out.println("""
                
                ╔══════════════════════════════════╗
                ║       User Service (CRUD)        ║
                ╠══════════════════════════════════╣
                ║  1. Создать пользователя         ║
                ║  2. Найти пользователя по ID     ║
                ║  3. Обновить пользователя        ║
                ║  4. Удалить пользователя         ║
                ║  5. Показать всех пользователей  ║
                ║  0. Выход                        ║
                ╚══════════════════════════════════╝
                """);
    }

    public static void main(String[] args) {
        var sessionFactory = com.crud.util.HibernateUtil.getSessionFactory();
        var userRepository = new com.crud.repository.UserRepositoryImpl(sessionFactory);
        var userService = new com.crud.service.UserServiceImpl(userRepository);
        var userController = new com.crud.controller.UserControllerImpl(userService);
        Console console = new Console(userController);
        console.start();
        com.crud.util.HibernateUtil.shutdown();
    }
}