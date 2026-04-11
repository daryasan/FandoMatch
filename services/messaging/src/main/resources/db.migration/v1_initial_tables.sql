create table if not exists media_item (
    media_id   text primary key,
    media_type text not null,
    created_at timestamp not null default now()
);

create table if not exists messaging_user (
    user_id    uuid primary key,
    username   text not null unique,
    name       text,
    updated_at timestamp not null default now()
);

create table if not exists chat (
    id         uuid primary key default gen_random_uuid(),
    user_id_1  uuid not null,
    user_id_2  uuid not null,
    created_at timestamp not null default now(),

    constraint chat_unique_pair unique (user_id_1, user_id_2),
    constraint chat_ordered_ids check (user_id_1 < user_id_2)
);

create index if not exists chat_idx_user1 on chat (user_id_1);
create index if not exists chat_idx_user2 on chat (user_id_2);

create table if not exists message (
    id         uuid primary key default gen_random_uuid(),
    chat_id    uuid not null,
    sender_id  uuid not null,
    content    text not null,
    media_ids  text[] not null default '{}',
    timestamp  bigint not null,
    is_read    boolean not null default false,
    created_at timestamp not null default now(),

    constraint message_chat_fk
        foreign key (chat_id) references chat (id)
            on update cascade on delete cascade
);

create index if not exists message_idx_chat     on message (chat_id, timestamp desc);
create index if not exists message_idx_unread   on message (chat_id, sender_id, is_read);
