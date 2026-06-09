package com.crud.notification.outbox;

/** Статус записи transactional outbox. */
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
