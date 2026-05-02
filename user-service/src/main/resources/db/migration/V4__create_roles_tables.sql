CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_role_user_id ON user_role(user_id);
CREATE INDEX idx_user_role_role_id ON user_role(role_id);

COMMENT ON TABLE roles IS 'Роли пользователей';
COMMENT ON COLUMN roles.id IS 'Идентификатор роли';
COMMENT ON COLUMN roles.name IS 'Название роли (USER, ADMIN и т.д.)';
COMMENT ON COLUMN roles.created_at IS 'Дата создания';
COMMENT ON COLUMN roles.updated_at IS 'Дата обновления';
COMMENT ON TABLE user_role IS 'Связь пользователей с ролями';
COMMENT ON COLUMN user_role.user_id IS 'ID пользователя';
COMMENT ON COLUMN user_role.role_id IS 'ID роли';
