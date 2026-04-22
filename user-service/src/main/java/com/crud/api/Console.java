package com.crud.api;

import com.crud.api.command.*;
import com.crud.controller.*;
import com.crud.repository.*;
import com.crud.service.*;
import com.crud.util.HibernateUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Главный класс консольного приложения.
 * <p>
 * Реализует паттерн Command для обработки действий пользователя.
 * Управление потоком выполнено через функциональный {@link Stream}.
 * </p>
 */
public class Console {
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
                3, new UpdateUserCommand(controller, scanner),
                4, new DeleteUserCommand(controller, scanner),
                5, new ListUsersCommand(controller),
                0, new ExitCommand()
        );
    }

    /**
     * Запускает основной цикл приложения.
     * Использует бесконечный поток, который завершается при получении {@code false}
     * от команды выхода.
     */
    public void start() {
        Stream.iterate(true, running -> running)
                .map(unused -> {
                    printMenu();
                    return true;
                })
                .map(unused -> ConsoleInput.readInt(scanner, "Ваш выбор: "))
                .map(commands::get)
                .filter(Objects::nonNull)
                .map(Command::execute)
                .filter(continueRunning -> !continueRunning)
                .findFirst()
                .ifPresent(stop -> {
                    scanner.close();
                    HibernateUtil.shutdown();
                });
    }

    /**
     * Выводит на экран главное меню.
     */
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

    /**
     * Точка входа в приложение. Собирает зависимости и запускает консоль.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        var sessionFactory = HibernateUtil.getSessionFactory();
        UserRepository userRepository = new UserRepositoryImpl(sessionFactory);
        UserService userService = new UserServiceImpl(userRepository);
        UserController userController = new UserControllerImpl(userService);
        new Console(userController).start();
    }
}