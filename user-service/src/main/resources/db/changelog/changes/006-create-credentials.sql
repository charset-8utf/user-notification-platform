--liquibase formatted sql

--changeset system:006-create-credentials
CREATE TABLE credentials (
    id         BIGSERIAL PRIMARY KEY NOT NULL,
    user_id    BIGINT NOT NULL UNIQUE,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_credentials_username ON credentials (username);

COMMENT ON TABLE credentials IS 'Учётные данные для аутентификации';
