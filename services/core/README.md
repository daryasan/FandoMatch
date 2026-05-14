# Сервис Core

## Коды ошибок

| Ручка                                     | Ошибки                                                                                               |
|-------------------------------------------|------------------------------------------------------------------------------------------------------|
| `POST /core/user/profile`                 | `200 + USER_NOT_FOUND`, `200 + PROFILE_INCOMPLETE`;  `401`, `503`                                    |
| `PATCH /core/user/profile/edit`           | `200 + USER_NOT_FOUND`, `200 + PROFILE_INCOMPLETE`, `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503` |
| `GET /core/user/profile/pending_requests` | `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503`                                                     |
| `GET /core/user/profile/friends`          | `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503`                                                     |
| `GET /core/user/preferences`              | `401`, `503`                                                                                         |
| `PATCH /core/user/preferences`            | `401`, `503`                                                                                         |
| `POST /core/match/next`                   | `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503`                                                     |
| `POST /core/match/react`                  | `200 + USER_NOT_FOUND`, `200 + ALREADY_REACTED`;  `401`, `503`                                       |
| `GET /core/match/get_current_filters`     | `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503`                                                     |
| `POST /core/match/filter`                 | `401`, `503`                                                                                         |
| `POST /core/match/internal/exists`        | `400`, `403`                                                                                         |
| `POST /core/posts/get`                    | `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND`                                            |
| `POST /core/posts/create`                 | `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503`                             |
| `GET /core/posts/{post_id}`               | `200 + POST_NOT_FOUND`, `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND`                    |
| `GET /core/posts/{post_id}/comments`      | `200 + POST_NOT_FOUND`                                                                               |
| `POST /core/posts/{post_id}/like`         | `200 + POST_NOT_FOUND`;  `401`, `503`                                                                |
| `POST /core/posts/{post_id}/comment`      | `200 + POST_NOT_FOUND`;  `401`, `503`                                                                |
| `GET /core/feed`                          | `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503`                             |
| `GET /core/fandoms/search`                | `200 + FANDOM_CATEGORY_NOT_FOUND`;  `401`, `503`                                                     |
| `GET /core/cities/search`                 | `401`, `503`                                                                                         |
| `POST /core/fandoms/user`                 | `200 + FANDOM_CATEGORY_NOT_FOUND`                                                                    |
| `GET /core/fandoms/categories`            | -                                                                                                    |
| `POST /core/fandoms/request-new`          | -                                                                                                    |