package com.crud.api.command.profile;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.api.command.Confirmation;
import com.crud.controller.ProfileController;

@Slf4j
public class DeleteProfileCommand implements Command {
    private final ProfileController profileController;
    private final ConsoleInput consoleInput;

    public DeleteProfileCommand(ProfileController profileController, ConsoleInput consoleInput) {
        this.profileController = profileController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long userId = consoleInput.readLong("Введите ID пользователя: ");
        String confirm = consoleInput.readString("Вы уверены? (y/n): ", "n");
        if (!Confirmation.isConfirmed(confirm)) {
            log.info("Удаление отменено.");
            return;
        }
        try {
            profileController.deleteProfile(userId);
            log.info("Профиль пользователя ID {} удалён", userId);
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
