--liquibase formatted sql

--changeset system:001-create-users
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY NOT NULL,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    age        INTEGER NOT NULL,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_age CHECK (age >= 0 AND age <= 150)
);

CREATE INDEX idx_users_created_at ON users (created_at);

COMMENT ON TABLE users IS 'Зарегистрированные пользователи системы';
