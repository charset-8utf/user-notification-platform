package com.crud.api.command;

import com.crud.api.ConsoleInput;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import org.slf4j.Logger;

/**
 * Базовый класс для команд отображения списка с пагинацией.
 *
 * @param <T> тип элемента в списке
 */
public abstract class PagedListCommand<T> implements Command {
    protected static final int DEFAULT_PAGE_SIZE = 5;

    protected final ConsoleInput consoleInput;
    protected final PagedConsoleSupport pagedConsoleSupport;
    protected final int pageSize;

    protected PagedListCommand(ConsoleInput consoleInput, int pageSize, PagedConsoleSupport pagedConsoleSupport) {
        this.consoleInput = consoleInput;
        this.pagedConsoleSupport = pagedConsoleSupport;
        this.pageSize = Math.clamp(pageSize, 1, 100);
    }

    @Override
    public void execute() {
        int currentPage = 0;
        boolean viewing = true;

        while (viewing) {
            try {
                Page<T> page = fetchPage(Pageable.of(currentPage, pageSize));

                if (page.isEmpty()) {
                    getLogger().info("Список пуст.");
                    return;
                }

                displayHeader(currentPage, page);
                displayContent(page);

                int choice = consoleInput.readInt(pagedConsoleSupport.buildOptions(page));

                switch (choice) {
                    case 1, 2 -> currentPage = pagedConsoleSupport.resolveNextPage(currentPage, choice, page, getLogger());
                    case 0 -> viewing = false;
                    default -> getLogger().error("Неверный выбор.");
                }
            } catch (RuntimeException e) {
                getLogger().error("Ошибка: {}", e.getMessage(), e);
                return;
            }
        }
    }

    protected abstract Logger getLogger();

    protected abstract Page<T> fetchPage(Pageable pageable);

    protected void displayHeader(int currentPage, Page<T> page) {
        getLogger().info("Страница {} из {}:", currentPage + 1, page.totalPages());
    }

    protected abstract void displayContent(Page<T> page);
}
