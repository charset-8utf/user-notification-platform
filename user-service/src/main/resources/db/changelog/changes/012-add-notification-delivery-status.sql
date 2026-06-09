--liquibase formatted sql
--changeset platform:012-add-notification-delivery-status
ALTER TABLE users ADD COLUMN notification_delivery_status VARCHAR(32) NOT NULL DEFAULT 'PENDING';
