package com.notification.domain;

/** Статус записи transactional inbox. */
public enum InboxStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}
