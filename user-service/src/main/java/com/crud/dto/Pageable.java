package com.crud.dto;

/**
 * Параметры пагинации.
 */
public record Pageable(int page, int size) {
    /**
     * Создаёт Pageable с валидацией.
     */
    public static Pageable of(int page, int size) {
        int validatedPage = Math.max(0, page);
        int validatedSize = Math.clamp(size, 1, 100);
        return new Pageable(validatedPage, validatedSize);
    }

    /**
     * Смещение для SQL-запроса.
     */
    public int offset() {
        return page * size;
    }
}
