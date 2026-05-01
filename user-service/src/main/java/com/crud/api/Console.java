package com.crud.api;

import com.crud.api.command.Command;
import com.crud.api.command.CommandRegistry;
import com.crud.api.command.factory.MainMenuCommandFactory;
import com.crud.api.command.factory.NotesMenuCommandFactory;
import com.crud.api.command.factory.ProfilesMenuCommandFactory;
import com.crud.api.command.factory.RolesMenuCommandFactory;
import com.crud.api.command.factory.UsersMenuCommandFactory;
import com.crud.ApplicationBuilder;
import com.crud.controller.NoteController;
import com.crud.controller.ProfileController;
import com.crud.controller.RoleController;
import com.crud.controller.UserController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;

/**
 * Консольное приложение с многоуровневым меню.
 */
@Slf4j
public class Console {
    private final ConsoleInput consoleInput;
    private final MenuRouter menuRouter;
    private final MenuViewProvider menuViewProvider;
    private final CommandRegistry commandRegistry;

    public Console(UserController userController,
                   NoteController noteController,
                   RoleController roleController,
                   ProfileController profileController) {
        this.consoleInput = new ConsoleInput(new Scanner(System.in));
        this.menuRouter = new MenuRouter();
        this.menuViewProvider = new MenuViewProvider();
        this.commandRegistry = new CommandRegistry(List.of(
                new MainMenuCommandFactory(menuRouter),
                new UsersMenuCommandFactory(userController, consoleInput),
                new NotesMenuCommandFactory(noteController, consoleInput),
                new RolesMenuCommandFactory(roleController, consoleInput),
                new ProfilesMenuCommandFactory(profileController, consoleInput)
        ));
    }

    /**
     * Запускает главный цикл.
     */
    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = consoleInput.readInt("Ваш выбор: ");
            if (choice == 0) {
                running = menuRouter.backOrExit();
                continue;
            }

            Command cmd = commandRegistry.get(menuRouter.currentMenu(), choice);
            if (cmd != null) {
                cmd.execute();
            } else {
                log.error("Неверный выбор. Введите число из меню.");
            }
        }
    }

    private void printMenu() {
        String menuText = menuViewProvider.menuText(menuRouter.currentMenu());
        log.info(menuText);
    }

    /**
     * Точка входа.
     */
    public static void main(String[] args) {
        var context = new ApplicationBuilder().build();
        Console console = new Console(
                context.userController(),
                context.noteController(),
                context.roleController(),
                context.profileController()
        );
        try {
            console.start();
        } finally {
            context.hibernateUtil().shutdown();
        }
    }
}
