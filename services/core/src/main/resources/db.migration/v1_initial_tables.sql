create table if not exists user_profile (
    user_id          uuid primary key,
    username         text not null unique,
    name             TEXT,
    bio              text,
    avatar_media_id  text,
    background_media_id text,
    gender           text,
    birth_date       date,
    city             text,
    updated_at       timestamp not null default now()
);

create table if not exists fandom_category (
    id   uuid primary key default gen_random_uuid(),
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS fandom (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id uuid not null,
    name        TEXT NOT NULL,
    description text,

    constraint fandom_category_fk
        foreign key (category_id) references fandom_category (id)
            on update CASCADE ON delete CASCADE,

    CONSTRAINT fandom_unique_name_per_category
        UNIQUE (category_id, name)
);

create index if not exists fandom_idx_category on fandom (category_id);

create table if not exists user_fandom (
    user_id   uuid not null,
    fandom_id uuid not null,

    primary key (user_id, fandom_id),

    constraint user_fandom_fandom_fk
        foreign key (fandom_id) references fandom (id)
            on update CASCADE ON delete CASCADE
);

create index if not exists user_fandom_idx_user on user_fandom (user_id);

create table if not exists fandom_request (
    id              uuid primary key default gen_random_uuid(),
    name            TEXT NOT NULL,
    description     text,
    category        text,
    author_username text not null,
    status          text not null default 'PENDING'
        check (status in ('PENDING', 'APPROVED', 'REJECTED')),
    created_at      timestamp not null default now()
);

create table if not exists post (
    id         uuid primary key default gen_random_uuid(),
    author_id  uuid not null,
    fandom_id  uuid,
    title      text not null,
    content    text not null,
    media_ids  text[] not null default '{}',
    created_at timestamp not null default now(),
    updated_at timestamp,

    constraint post_fandom_fk
        foreign key (fandom_id) references fandom (id)
            on update CASCADE ON delete SET NULL
);

create index if not exists post_idx_author  on post (author_id);
create index if not exists post_idx_fandom  on post (fandom_id);
create index if not exists post_idx_created on post (created_at desc);

create table if not exists comment (
    id         uuid primary key default gen_random_uuid(),
    post_id    uuid not null,
    author_id  uuid not null,
    content    text not null,
    created_at timestamp not null default now(),

    constraint comment_post_fk
        foreign key (post_id) references post (id)
            on update CASCADE ON delete CASCADE
);

create index if not exists comment_idx_post on comment (post_id, created_at);

create table if not exists post_like (
    user_id    uuid not null,
    post_id    uuid not null,
    created_at timestamp not null default now(),

    primary key (user_id, post_id),

    constraint post_like_post_fk
        foreign key (post_id) references post (id)
            on update CASCADE ON delete CASCADE
);

create index if not exists post_like_idx_post on post_like (post_id);

create table if not exists match_action (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null,
    target_user_id uuid not null,
    action         text not null check (action in ('LIKE', 'DISLIKE')),
    created_at     timestamp not null default now(),

    constraint match_action_unique_pair
        unique (user_id, target_user_id)
);

CREATE INDEX IF NOT EXISTS match_action_idx_user   ON match_action (user_id);
create index if not exists match_action_idx_target on match_action (target_user_id);

create table if not exists match (
    id         uuid primary key default gen_random_uuid(),
    user_id_1  uuid not null,
    user_id_2  uuid not null,
    matched_at timestamp not null default now(),

    constraint match_unique_pair unique (user_id_1, user_id_2),
    constraint match_ordered_ids check  (user_id_1 < user_id_2)
);

create index if not exists match_idx_user1 on match (user_id_1);
create index if not exists match_idx_user2 on match (user_id_2);

create table if not exists match_pending (
    id                uuid primary key default gen_random_uuid(),
    user_id           uuid not null,
    suggested_user_id uuid not null,
    created_at        timestamp not null default now(),

    constraint match_pending_unique_pair
        unique (user_id, suggested_user_id)
);

create index if not exists match_pending_idx_user on match_pending (user_id);

create table if not exists match_filter (
    user_id         uuid primary key,
    gender          text,
    age_from        integer,
    age_to          integer,
    city            text,
    fandom_category uuid,
    fandom_id       uuid,

    constraint match_filter_category_fk
        foreign key (fandom_category) references fandom_category (id),
    constraint match_filter_fandom_fk
        foreign key (fandom_id) references fandom (id)
);
