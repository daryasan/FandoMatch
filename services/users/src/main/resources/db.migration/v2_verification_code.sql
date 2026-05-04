CREATE TABLE IF NOT EXISTS verification_code (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      TEXT NOT NULL,
    code       VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used       BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS verification_code_idx_email ON verification_code (email);

ALTER TABLE "user"
    DROP CONSTRAINT IF EXISTS user_status_check,
    ADD CONSTRAINT user_status_check CHECK (status IN ('ACTIVE','DISABLED','BANNED','DELETED'));
