CREATE TABLE IF NOT EXISTS tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  token_hash TEXT NOT NULL UNIQUE,
  issued_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS token_index_user_id ON tokens(user_id);

ALTER TABLE tokens
  ADD CONSTRAINT tokens_user_fk
  FOREIGN KEY (user_id) REFERENCES "user"(internal_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;

COMMENT ON TABLE tokens IS 'Таблица с токенами пользователей';



CREATE TABLE IF NOT EXISTS user_credentials (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE,
  credential_type TEXT NOT NULL,
  hash TEXT NOT NULL,
  salt TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS user_credentials_index_user_id ON user_credentials(user_id);

ALTER TABLE user_credentials
  ADD CONSTRAINT user_credentials_user_fk
  FOREIGN KEY (user_id) REFERENCES "user"(internal_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;

COMMENT ON TABLE user_credentials IS 'Информация о входе пользователя';



CREATE TABLE IF NOT EXISTS "user" (
  internal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE,
  phone TEXT UNIQUE,
  username TEXT NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  CONSTRAINT user_status_check CHECK (status IN ('ACTIVE','DISABLED','BANNED'))
);

CREATE INDEX IF NOT EXISTS user_index_internal_id ON "user"(internal_id);
CREATE INDEX IF NOT EXISTS user_index_username ON "user"(username);
COMMENT ON TABLE "user" IS 'Таблица с минимальными регистрационными данными пользователя';

