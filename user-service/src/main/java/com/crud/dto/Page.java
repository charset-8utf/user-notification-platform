package com.crud.dto;

import java.util.List;

/**
 * Страница результатов с пагинацией.
 */
public record Page<T>(List<T> content, long totalElements, int page, int size) {
    public Page {
        content = content == null ? List.of() : List.copyOf(content);
    }

    /**
     * Общее количество страниц.
     */
    public int totalPages() {
        if (size == 0) return 0;
        return (int) Math.ceil((double) totalElements / size);
    }

    /**
     * Проверяет наличие следующей страницы.
     */
    public boolean hasNext() {
        return page + 1 < totalPages();
    }

    /**
     * Проверяет наличие предыдущей страницы.
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * Проверяет, пуста ли страница.
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }
}
