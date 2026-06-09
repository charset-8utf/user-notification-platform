--liquibase formatted sql

--changeset system:009-create-credentials-updated-at-trigger
CREATE TRIGGER update_credentials_updated_at
    BEFORE UPDATE ON credentials
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
