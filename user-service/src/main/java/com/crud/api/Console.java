package com.crud.api;

import com.crud.api.command.*;
import com.crud.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Scanner;

/**
 * Главный класс консольного приложения.
 * <p>
 * Реализует паттерн Command для обработки действий пользователя.
 * Управление циклом происходит через флаг {@code running}.
 * </p>
 */
public class Console {
    private static final Logger log = LoggerFactory.getLogger(Console.class);
    private final Map<Integer, Command> commands;
    private final Scanner scanner;

    /**
     * Конструктор, инициализирующий команды и сканер.
     *
     * @param controller контроллер пользователей (внедрение зависимости)
     */
    public Console(UserController controller) {
        this.scanner = new Scanner(System.in);
        this.commands = Map.of(
                1, new CreateUserCommand(controller, scanner),
                2, new FindUserCommand(controller, scanner),
                3, new FindByEmailCommand(controller, scanner),
                4, new UpdateUserCommand(controller, scanner),
                5, new DeleteUserCommand(controller, scanner),
                6, new ListUsersCommand(controller),
                0, new ExitCommand()
        );
    }

    /**
     * Запускает основной цикл приложения.
     * Выводит меню, читает выбор пользователя и выполняет соответствующую команду.
     * Цикл завершается при выполнении команды {@link ExitCommand}.
     */
    public void start() {
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = ConsoleInput.readInt(scanner, "Ваш выбор: ");
                Command cmd = commands.get(choice);
                if (cmd == null) {
                    log.error("❌ Неверный выбор. Введите число от 0 до 6.");
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

    /**
     * Выводит на экран главное меню.
     */
    private void printMenu() {
        log.info("""
                
                ╔══════════════════════════════════╗
                ║       User Service (CRUD)        ║
                ╠══════════════════════════════════╣
                ║  1. Создать пользователя         ║
                ║  2. Найти пользователя по ID     ║
                ║  3. Найти пользователя по email  ║
                ║  4. Обновить пользователя        ║
                ║  5. Удалить пользователя         ║
                ║  6. Показать всех пользователей  ║
                ║  0. Выход                        ║
                ╚══════════════════════════════════╝
                """);
    }

    /**
     * Точка входа в приложение.
     * Собирает зависимости и запускает консоль.
     *
     * @param args аргументы командной строки (не используются)
     */
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