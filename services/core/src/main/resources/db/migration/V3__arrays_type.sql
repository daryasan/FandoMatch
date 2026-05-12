ALTER TABLE match_filter
    ALTER COLUMN gender TYPE text[] USING gender::text[],
    ALTER COLUMN fandom_categories TYPE text[] USING fandom_categories::text[],
    ALTER COLUMN fandom_ids TYPE text[] USING fandom_ids::text[];