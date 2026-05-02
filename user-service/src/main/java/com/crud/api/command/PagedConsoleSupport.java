package com.crud.api.command;

import com.crud.dto.Page;
import org.slf4j.Logger;

/**
 * Утилита для постраничного вывода в консоли.
 */
public class PagedConsoleSupport {
    public String buildOptions(Page<?> page) {
        StringBuilder options = new StringBuilder();
        if (page.hasNext()) {
            options.append("[1] Следующая страница ");
        }
        if (page.hasPrevious()) {
            options.append("[2] Предыдущая страница ");
        }
        options.append("[0] Назад");
        return options.toString();
    }

    /**
     * Определяет следующую страницу на основе выбора.
     */
    public int resolveNextPage(int currentPage, int choice, Page<?> page, Logger log) {
        return switch (choice) {
            case 1 -> {
                if (page.hasNext()) {
                    yield currentPage + 1;
                }
                log.info("Это последняя страница.");
                yield currentPage;
            }
            case 2 -> {
                if (page.hasPrevious()) {
                    yield currentPage - 1;
                }
                log.info("Это первая страница.");
                yield currentPage;
            }
            default -> currentPage;
        };
    }
}
