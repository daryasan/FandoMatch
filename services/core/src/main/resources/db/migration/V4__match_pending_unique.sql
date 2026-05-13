DELETE FROM match_pending mp
WHERE mp.id NOT IN (
    SELECT DISTINCT ON (user_id, suggested_user_id) id
    FROM match_pending
    ORDER BY user_id, suggested_user_id, created_at ASC
);

ALTER TABLE match_pending
    ADD CONSTRAINT match_pending_user_id_suggested_user_id_key UNIQUE (user_id, suggested_user_id);
