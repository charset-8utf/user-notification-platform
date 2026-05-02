package com.crud.api.command.profile;

import lombok.extern.slf4j.Slf4j;
import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.ProfileController;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;

@Slf4j
public class CreateProfileCommand implements Command {
    private final ProfileController profileController;
    private final ConsoleInput consoleInput;

    public CreateProfileCommand(ProfileController profileController, ConsoleInput consoleInput) {
        this.profileController = profileController;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long userId = consoleInput.readLong("Введите ID пользователя: ");
        String phone = consoleInput.readString("Введите телефон (Enter - пропустить): ", "");
        String address = consoleInput.readString("Введите адрес (Enter - пропустить): ", "");
        try {
            ProfileResponse profile = profileController.createProfile(userId, new ProfileRequest(phone, address));
            if (log.isInfoEnabled()) {
                log.info("Профиль создан для пользователя ID {}: Телефон={}, Адрес={}",
                        userId, profile.phone(), profile.address());
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}
