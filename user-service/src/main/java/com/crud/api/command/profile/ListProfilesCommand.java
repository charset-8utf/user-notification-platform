package com.crud.api.command.profile;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import com.crud.api.ConsoleInput;
import com.crud.api.command.PagedConsoleSupport;
import com.crud.api.command.PagedListCommand;
import com.crud.controller.ProfileController;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.ProfileResponse;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

/**
 * Команда для отображения списка профилей с пагинацией.
 */
@Slf4j
public class ListProfilesCommand extends PagedListCommand<ProfileResponse> {

    private final ProfileController profileController;
    private final UserMapper userMapper;

    public ListProfilesCommand(ProfileController profileController, ConsoleInput consoleInput) {
        this(profileController, consoleInput, DEFAULT_PAGE_SIZE);
    }

    public ListProfilesCommand(ProfileController profileController, ConsoleInput consoleInput, int pageSize) {
        this(profileController, consoleInput, pageSize, new PagedConsoleSupport());
    }

    public ListProfilesCommand(ProfileController profileController,
                               ConsoleInput consoleInput,
                               int pageSize,
                               PagedConsoleSupport pagedConsoleSupport) {
        super(consoleInput, pageSize, pagedConsoleSupport);
        this.profileController = profileController;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected void displayHeader(int currentPage, Page<ProfileResponse> page) {
        log.info("Профили (страница {} из {}):", currentPage + 1, page.totalPages());
    }

    @Override
    protected Page<ProfileResponse> fetchPage(Pageable pageable) {
        return profileController.findAllProfiles(pageable);
    }

    @Override
    protected void displayContent(Page<ProfileResponse> page) {
        if (log.isInfoEnabled()) {
            page.content().forEach(profile ->
                log.info("   ID: {} | User ID: {} | Телефон: {} | Адрес: {} | Создан: {}",
                        profile.id(),
                        profile.userId(),
                        profile.phone(),
                        profile.address(),
                        userMapper.formatDateTime(profile.createdAt())));
        }
    }
}
