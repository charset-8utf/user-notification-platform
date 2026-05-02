package com.crud.api.command.profile;

import lombok.extern.slf4j.Slf4j;

import com.crud.api.ConsoleInput;
import com.crud.api.command.Command;
import com.crud.controller.ProfileController;
import com.crud.dto.ProfileResponse;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

@Slf4j
public class GetProfileCommand implements Command {
    private final ProfileController profileController;
    private final ConsoleInput consoleInput;
    private final UserMapper userMapper;

    public GetProfileCommand(ProfileController profileController, ConsoleInput consoleInput) {
        this.profileController = profileController;
        this.consoleInput = consoleInput;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    public void execute() {
        long userId = consoleInput.readLong("Введите ID пользователя: ");
        try {
            ProfileResponse profile = profileController.findProfileByUserId(userId);
            if (log.isInfoEnabled()) {
                log.info("""
                        Профиль пользователя ID {}:
                           Телефон: {}
                           Адрес: {}
                           Создан: {}
                           Обновлён: {}
                        """, userId, profile.phone(), profile.address(),
                        userMapper.formatDateTime(profile.createdAt()),
                        userMapper.formatDateTime(profile.updatedAt()));
            }
        } catch (RuntimeException e) {
            log.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}