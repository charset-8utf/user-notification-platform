package com.crud.notification;

/**
 * Абстракция канала доставки нотификаций о пользовательских событиях.
 * Реализации выбираются по активному профилю: {@code kafka} (Kafka producer) или {@code rest}
 * (синхронный REST к notification-service, будет добавлен следующим шагом).
 * По умолчанию — no-op, чтобы локальная разработка и тесты не требовали инфраструктуры.
 */
public interface UserNotificationPort {

    void publish(UserNotificationEvent event);
}
