ALTER TABLE users ADD COLUMN updated_at TIMESTAMP;
COMMENT ON COLUMN users.updated_at IS 'Дата и время последнего обновления записи';
UPDATE users SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE users ALTER COLUMN updated_at SET NOT NULL;
