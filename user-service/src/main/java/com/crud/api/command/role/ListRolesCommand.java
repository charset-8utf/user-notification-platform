package com.crud.api.command.role;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import com.crud.api.ConsoleInput;
import com.crud.api.command.PagedConsoleSupport;
import com.crud.api.command.PagedListCommand;
import com.crud.controller.RoleController;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.RoleResponse;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;

/**
 * Команда для отображения списка ролей с пагинацией.
 */
@Slf4j
public class ListRolesCommand extends PagedListCommand<RoleResponse> {

    private final RoleController roleController;
    private final UserMapper userMapper;

    public ListRolesCommand(RoleController roleController, ConsoleInput consoleInput) {
        this(roleController, consoleInput, DEFAULT_PAGE_SIZE);
    }

    public ListRolesCommand(RoleController roleController, ConsoleInput consoleInput, int pageSize) {
        this(roleController, consoleInput, pageSize, new PagedConsoleSupport());
    }

    public ListRolesCommand(RoleController roleController,
                            ConsoleInput consoleInput,
                            int pageSize,
                            PagedConsoleSupport pagedConsoleSupport) {
        super(consoleInput, pageSize, pagedConsoleSupport);
        this.roleController = roleController;
        this.userMapper = new UserMapperImpl();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected void displayHeader(int currentPage, Page<RoleResponse> page) {
        log.info("Роли (страница {} из {}):", currentPage + 1, page.totalPages());
    }

    @Override
    protected Page<RoleResponse> fetchPage(Pageable pageable) {
        return roleController.findAllRoles(pageable);
    }

    @Override
    protected void displayContent(Page<RoleResponse> page) {
        if (log.isInfoEnabled()) {
            page.content().forEach(role ->
                log.info("   ID: {} | Название: {} | Создана: {}",
                        role.id(), role.name(),
                        userMapper.formatDateTime(role.createdAt())));
        }
    }
}
