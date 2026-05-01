package com.crud.api.command;

import com.crud.api.MenuRouter;
import com.crud.api.MenuState;

/**
 * Команда перехода в указанное меню.
 */
public class NavigateMenuCommand implements Command {
    private final MenuRouter menuRouter;
    private final MenuState target;

    public NavigateMenuCommand(MenuRouter menuRouter, MenuState target) {
        this.menuRouter = menuRouter;
        this.target = target;
    }

    @Override
    public void execute() {
        menuRouter.navigateTo(target);
    }
}
