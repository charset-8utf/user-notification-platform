--liquibase formatted sql

--changeset system:010-seed-roles
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('USER');
