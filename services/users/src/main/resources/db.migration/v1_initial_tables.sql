create TABLE IF NOT EXISTS tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  token_type TEXT NOT NULL CHECK (token_type IN ('ACCESS', 'REFRESH')),
  token_value TEXT NOT NULL UNIQUE,
  issued_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT false
);


create index IF NOT EXISTS token_index_user_id ON tokens(user_id);

alter table tokens
  add CONSTRAINT tokens_user_fk
  FOREIGN KEY (user_id) REFERENCES "user"(uid)
  ON update NO ACTION ON delete CASCADE;

comment on table tokens is 'Таблица с токенами пользователей';



create TABLE IF NOT EXISTS user_credentials (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  credential_type TEXT NOT NULL,
  hash TEXT NOT NULL,
  salt TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP
);

create index IF NOT EXISTS user_credentials_index_user_id ON user_credentials(user_id);

alter table user_credentials
  add CONSTRAINT user_credentials_user_fk
  FOREIGN KEY (user_id) REFERENCES "user"(uid)
  ON update NO ACTION ON delete CASCADE;

comment on table user_credentials is 'Информация о входе пользователя';



create TABLE IF NOT EXISTS "user" (
  uid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE,
  phone TEXT UNIQUE,
  username TEXT NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  CONSTRAINT user_status_check CHECK (status IN ('ACTIVE','DISABLED','BANNED'))
);

create index IF NOT EXISTS user_index_uid ON "user"(uid);
create index IF NOT EXISTS user_index_username ON "user"(username);
comment on table "user" is 'Таблица с минимальными регистрационными данными пользователя';

