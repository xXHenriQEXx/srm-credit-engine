-- V2__add_users_and_security.sql
-- Modulo de autenticacao: usuarios da plataforma (operadores/administradores)

CREATE TABLE app_user (
    id             UUID PRIMARY KEY,
    username       VARCHAR(60) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    role           VARCHAR(20) NOT NULL,
    enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_app_user_username ON app_user (username);

-- Usuario administrador inicial. Login: admin / Senha: admin
-- ATENCAO: troque a senha em qualquer ambiente que nao seja local/dev -
-- este seed existe apenas para permitir o primeiro acesso e a criacao
-- dos demais usuarios via /api/v1/auth/register (protegido por ADMIN).
-- Hash gerado com BCrypt (custo 10) para a senha "admin".
INSERT INTO app_user (id, username, password_hash, role, enabled, created_at)
VALUES (
    gen_random_uuid(),
    'admin',
    '$2b$10$/qu3.ogrMd4uUMXiy0uVU.YHph8vtIdavITZ5hOq7scqET3ehnYAe',
    'ADMIN',
    TRUE,
    now()
);
