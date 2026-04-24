package com.crud.util;

/**
 * Утилитарный класс для проверки работоспособности Hibernate SessionFactory.
 * <p>
 * Используется в Docker-образе в качестве healthcheck.
 * При успешном получении SessionFactory завершается с кодом 0,
 * при любом исключении – с кодом 1.
 * </p>
 * <p>
 * Запуск: {@code java -cp app.jar com.crud.util.HealthCheck}
 * </p>
 *
 * @author charset-8utf
 * @version 1.0
 * @since 2026-04-24
 */
public final class HealthCheck {

    private HealthCheck() {
        throw new UnsupportedOperationException("Утилитарный класс, создание экземпляров запрещено");
    }

    /**
     * Точка входа для healthcheck.
     * Получает SessionFactory, игнорируя возвращаемое значение,
     * но фиксируя успешное завершение.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try {
            if (HibernateUtil.getSessionFactory() == null) {
                System.exit(1);
            }
            System.exit(0);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}