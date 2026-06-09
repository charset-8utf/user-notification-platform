--liquibase formatted sql

--changeset system:005-create-profiles
CREATE TABLE profiles (
    id         BIGSERIAL PRIMARY KEY NOT NULL,
    phone      VARCHAR(20),
    address    TEXT,
    user_id    BIGINT NOT NULL UNIQUE,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_profiles_user_id ON profiles (user_id);

COMMENT ON TABLE profiles IS 'Профили пользователей (OneToOne)';
