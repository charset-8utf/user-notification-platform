CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20),
    address TEXT,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_profiles_user_id ON profiles(user_id);
COMMENT ON TABLE profiles IS 'Профили пользователей (OneToOne)';
COMMENT ON COLUMN profiles.phone IS 'Телефон';
COMMENT ON COLUMN profiles.address IS 'Адрес';
COMMENT ON COLUMN profiles.user_id IS 'ID пользователя (уникальный)';
