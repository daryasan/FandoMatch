CREATE TABLE IF NOT EXISTS messaging_user (
    user_id    UUID PRIMARY KEY,
    username   TEXT NOT NULL,
    name       TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id_1  UUID NOT NULL,
    user_id_2  UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chat_unique_pair UNIQUE (user_id_1, user_id_2)
);

CREATE INDEX IF NOT EXISTS chat_idx_user1 ON chat (user_id_1);
CREATE INDEX IF NOT EXISTS chat_idx_user2 ON chat (user_id_2);

CREATE TABLE IF NOT EXISTS message (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id    UUID NOT NULL,
    sender_id  UUID NOT NULL,
    content    TEXT NOT NULL,
    media_ids  TEXT[] NOT NULL DEFAULT '{}',
    timestamp  BIGINT NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT message_chat_fk
        FOREIGN KEY (chat_id) REFERENCES chat (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS message_idx_chat ON message (chat_id, timestamp DESC);

CREATE TABLE IF NOT EXISTS media_item (
    media_id   TEXT PRIMARY KEY,
    media_type TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
