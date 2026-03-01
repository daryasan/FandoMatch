create table if not exists user_profile (
  user_id uuid primary key,
  name text not null,
  bio text,
  avatar_url text,
  background_url text,
  gender text,
  birth_date date not null,
  city text,
  updated_at timestamp not null default now()
);

comment on table user_profile is 'Расширенная информация о пользователе (данные из core)';

create table if not exists fandom_category (
  id uuid primary key default gen_random_uuid(),
  name text not null unique
);

comment on table fandom_category is 'Категории фандомов';

create table if not exists fandom (
  id uuid primary key default gen_random_uuid(),
  category_id uuid not null,
  name text not null,
  description text,

  constraint fandom_category_fk
    foreign key (category_id) references fandom_category(id)
    on update cascade on delete cascade
);

create index if not exists fandom_index_category on fandom(category_id);

comment on table fandom is 'Фандомы';

create table if not exists user_fandom (
  user_id uuid not null,
  fandom_id uuid not null,

  primary key (user_id, fandom_id),

  constraint user_fandom_fandom_fk
    foreign key (fandom_id) references fandom(id)
    on update cascade on delete cascade
);

create index if not exists user_fandom_index_user on user_fandom(user_id);

comment on table user_fandom is 'Фандомы пользователя';


create table if not exists user_fandom (
  user_id uuid not null,
  fandom_id uuid not null,

  primary key (user_id, fandom_id),

  constraint user_fandom_fandom_fk
    foreign key (fandom_id) references fandom(id)
    on update cascade on delete cascade
);

create index if not exists user_fandom_index_user on user_fandom(user_id);

comment on table user_fandom is 'Фандомы пользователя';


create table if not exists comment (
  id uuid primary key default gen_random_uuid(),
  post_id uuid not null,
  author_id uuid not null,
  content text not null,
  created_at timestamp not null default now(),

  constraint comment_post_fk
    foreign key (post_id) references post(id)
    on update cascade on delete cascade
);

create index if not exists comment_index_post on comment(post_id);

comment on table comment is 'Комментарии к постам';

create table if not exists post_like (
  user_id uuid not null,
  post_id uuid not null,
  created_at timestamp not null default now(),

  primary key (user_id, post_id),

  constraint post_like_post_fk
    foreign key (post_id) references post(id)
    on update cascade on delete cascade
);

create index if not exists post_like_index_post on post_like(post_id);

comment on table post_like is 'Лайки постов';

create table if not exists match_action (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  target_user_id uuid not null,
  action text not null check (action in ('LIKE', 'DISLIKE')),
  created_at timestamp not null default now()
);

create index if not exists match_action_index_user on match_action(user_id);

create index if not exists match_action_index_target on match_action(target_user_id);

comment on table match_action is 'История лайков и дизлайков';

create table if not exists match_pending (
  user_id uuid primary key,
  suggested_user_id uuid not null,
  created_at timestamp not null default now()
);

comment on table match_pending is 'Текущий неоценённый кандидат';

create table if not exists match_filter (
  user_id uuid primary key,
  gender text,
  age_from integer,
  age_to integer,
  city text,
  fandom_category uuid,
  fandom_id uuid
);

comment on table match_filter is 'Фильтры подбора кандидатов';

create table if not exists fandom_request (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  description text,
  category text,
  author_username text not null,
  created_at timestamp not null default now()
);

comment on table fandom_request is 'Запросы на добавление новых фандомов';

