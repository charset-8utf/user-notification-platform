CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notes_user_id ON notes(user_id);
CREATE INDEX idx_notes_created_at ON notes(created_at);

COMMENT ON TABLE notes IS 'Заметки пользователей';
COMMENT ON COLUMN notes.id IS 'Уникальный идентификатор заметки';
COMMENT ON COLUMN notes.content IS 'Текст заметки';
COMMENT ON COLUMN notes.created_at IS 'Дата создания';
COMMENT ON COLUMN notes.updated_at IS 'Дата последнего обновления';
COMMENT ON COLUMN notes.user_id IS 'ID пользователя (внешний ключ)';
