CREATE TABLE IF NOT EXISTS user_preferences (
    user_id                         UUID PRIMARY KEY,
    match_notifications_enabled     BOOLEAN NOT NULL DEFAULT true,
    message_notifications_enabled   BOOLEAN NOT NULL DEFAULT true,
    hide_my_posts_from_non_matches  BOOLEAN NOT NULL DEFAULT false
);
