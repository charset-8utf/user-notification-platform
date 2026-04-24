CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    age INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT age_range CHECK (age >= 0 AND age <= 150)
);

COMMENT ON TABLE users IS 'Зарегистрированные пользователи системы';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор';
COMMENT ON COLUMN users.name IS 'Полное имя пользователя';
COMMENT ON COLUMN users.email IS 'Электронная почта';
COMMENT ON COLUMN users.age IS 'Возраст';
COMMENT ON COLUMN users.created_at IS 'Дата и время создания записи';

CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);