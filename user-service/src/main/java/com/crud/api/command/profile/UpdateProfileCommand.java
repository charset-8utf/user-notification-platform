package com.crud.api.command.profile;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.ProfileController;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;

@Slf4j
public class UpdateProfileCommand implements Command {
    private final ProfileController profileController;
    private final ConsoleInput consoleInput;

    public UpdateProfileCommand(ProfileController profileController, ConsoleInput consoleInput) {
        this.profileController = profileController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long userId = consoleInput.readLong("Введите ID пользователя: ");
        try {
            ProfileResponse existing = profileController.findProfileByUserId(userId);
            log.info("Текущий профиль: Телефон={}, Адрес={}", existing.phone(), existing.address());
            String phone = consoleInput.readString("Введите новый телефон (Enter - оставить \"" + existing.phone() + "\"): ", existing.phone());
            String address = consoleInput.readString("Введите новый адрес (Enter - оставить \"" + existing.address() + "\"): ", existing.address());
            profileController.updateProfile(userId, new ProfileRequest(phone, address));
            if (log.isInfoEnabled()) {
                log.info("Профиль обновлён для пользователя ID {}", userId);
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
