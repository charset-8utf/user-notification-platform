--liquibase formatted sql

--changeset system:011-create-notification-outbox
CREATE TABLE notification_outbox (
    id           BIGSERIAL PRIMARY KEY,
    event_id     UUID NOT NULL,
    operation    TEXT NOT NULL,
    email        TEXT NOT NULL,
    status       TEXT NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    CONSTRAINT uq_notification_outbox_event_id UNIQUE (event_id),
    CONSTRAINT chk_notification_outbox_operation_len CHECK (char_length(operation) <= 32),
    CONSTRAINT chk_notification_outbox_email_len CHECK (char_length(email) <= 150),
    CONSTRAINT chk_notification_outbox_status_len CHECK (char_length(status) <= 16),
    CONSTRAINT chk_notification_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_notification_outbox_status_created ON notification_outbox (status, created_at);

COMMENT ON TABLE notification_outbox IS 'Transactional outbox для событий user-notifications (Kafka)';
