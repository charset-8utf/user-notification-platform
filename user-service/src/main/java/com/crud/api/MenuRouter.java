package com.crud.api;

import lombok.extern.slf4j.Slf4j;

/**
 * Управление переходами между состояниями меню.
 */
@Slf4j
public class MenuRouter {

    private MenuState currentMenu = MenuState.MAIN;

    public MenuState currentMenu() {
        return currentMenu;
    }

    public void navigateTo(MenuState targetMenu) {
        currentMenu = targetMenu;
        log.info("Переход в меню: {}", targetMenu);
    }

    /**
     * Обрабатывает "0": выход или возврат в главное меню.
     */
    public boolean backOrExit() {
        if (currentMenu == MenuState.MAIN) {
            return false;
        }

        currentMenu = MenuState.MAIN;
        log.info("Возврат в главное меню.");
        return true;
    }
}
