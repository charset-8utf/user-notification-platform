package com.crud.api.command;

import com.crud.dto.Page;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PagedConsoleSupportTest {
    private final PagedConsoleSupport pagedConsoleSupport = new PagedConsoleSupport();

    @Test
    void buildOptions_WhenHasNextAndPrevious_ShouldIncludeBothOptions() {
        Page<String> page = new Page<>(List.of("a"), 3, 1, 1);

        String options = pagedConsoleSupport.buildOptions(page);

        assertTrue(options.contains("[1] Следующая страница"));
        assertTrue(options.contains("[2] Предыдущая страница"));
        assertTrue(options.contains("[0] Назад"));
    }

    @Test
    void resolveNextPage_ShouldReturnExpectedPageIndex() {
        Logger logger = mock(Logger.class);
        Page<String> page = new Page<>(List.of("a"), 3, 1, 1);

        int next = pagedConsoleSupport.resolveNextPage(1, 1, page, logger);
        int previous = pagedConsoleSupport.resolveNextPage(1, 2, page, logger);
        int unchanged = pagedConsoleSupport.resolveNextPage(1, 9, page, logger);

        assertEquals(2, next);
        assertEquals(0, previous);
        assertEquals(1, unchanged);
    }
}
