--liquibase formatted sql

--changeset system:002-create-notes
CREATE TABLE notes (
    id         BIGSERIAL PRIMARY KEY NOT NULL,
    content    TEXT NOT NULL,
    version    BIGINT NOT NULL DEFAULT 0,
    user_id    BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notes_user_id ON notes (user_id);
CREATE INDEX idx_notes_created_at ON notes (created_at);

COMMENT ON TABLE notes IS 'Заметки пользователей';
