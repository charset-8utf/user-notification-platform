--liquibase formatted sql

--changeset system:003-create-roles
CREATE TABLE roles (
    id         BIGSERIAL PRIMARY KEY NOT NULL,
    name       VARCHAR(50) NOT NULL UNIQUE,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE roles IS 'Роли пользователей';
