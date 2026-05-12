CREATE TABLE IF NOT EXISTS "user" (
    uid        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      TEXT NOT NULL UNIQUE,
    username   TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status     TEXT NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'DISABLED', 'BANNED', 'DELETED'))
);

CREATE TABLE IF NOT EXISTS user_credentials (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    credential_type TEXT NOT NULL CHECK (credential_type IN ('PASSWORD')),
    hash            TEXT NOT NULL DEFAULT '',
    salt            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT user_credentials_user_fk
        FOREIGN KEY (user_id) REFERENCES "user" (uid)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS user_credentials_idx_user ON user_credentials (user_id);

CREATE TABLE IF NOT EXISTS tokens (
    internal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    token_type  TEXT NOT NULL CHECK (token_type IN ('ACCESS', 'REFRESH')),
    token_value TEXT NOT NULL UNIQUE,
    issued_at   TIMESTAMP NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT tokens_user_fk
        FOREIGN KEY (user_id) REFERENCES "user" (uid)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS tokens_idx_user  ON tokens (user_id);
CREATE INDEX IF NOT EXISTS tokens_idx_value ON tokens (token_value);

CREATE TABLE IF NOT EXISTS device_token (
    user_id    UUID PRIMARY KEY,
    fcm_token  TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS verification_code (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      TEXT NOT NULL,
    code       VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS verification_code_idx_email ON verification_code (email);
