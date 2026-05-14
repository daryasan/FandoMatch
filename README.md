# FandoMatch
Дипломная работа студентки НИУ ВШЭ ФКН ПИ 2026 года Судаковой Дарьи Евгеньевны.

# Описание
Серверная часть проекта FandoMatch 

## Как запустить тесты
Командой ``./gradlew clean test jacocoTestReport``

## Как запустить DEV / LOCAL среду
0. _**Предусловия:**_  Наличие скачанных PGadmin, docker desktop, опциональьно bruno для запросов к серверу
1. Склонировать репозиторий
2. Сгенерировть API клиенты. Для этого из корня проекта запускаем команду  ``.\gradlew.bat openApiGenerate``
3. Создать базы данных. Для этого в PG Admin создаем БД с названиями и паролями ровно как указано в application.yaml каждого из сервисов в папке ``services``. Создаем таблицы с помощью SQL скриптов из папок `services/{service_name}/resources/db.migration`
4. Запускаем кафку с помощью команды из корня проекта ``docker compose up --build kafka``
5. Стартуем приложения в таком порядке: users, core, gateway. Для того чтобы нужный профиль подтянулся, делаем следующее: в конфигурации в Intelij Idea нажимаем `edit` -> `VM Options` -> вставляем строчку `-Dspring.profiles.active=local` -> `apply`  ![img.png](img.png) ![img_1.png](img_1.png)
   Так делаем для каждой конфигурации и потом запускаем. 
6. Локальный профиль готов! Можно делать запросы, вероятно только через postman/bruno

## Как запустить UAT среду
**ВАЖНО!** Обязательно нужно выключить все прокси/ВПН перед запуском, иначе не получится запустить докер.
0. _**Предусловия:**_ Запускаем/скачиваем [Ехидного кита для десктопа](https://www.docker.com/products/docker-desktop/).
1. Далее в корень проекта нужно положить файл под названием ``.env`` с секретами. Секреты я дам.
2. _**(Опционально, в случае ошибок в следующих пунктах - иначе можно смело скипать)**_ Убеждаемся что в папках ``clients/..`` лежит папка ``build``. Ее я не дам, ее нужно сгенерить командой ``.\gradlew.bat openApiGenerate``
2. Запускаем из корня проекта команду:
   - ``docker compose up --build`` - если билдим в первый раз
   - ``docker compose up`` - если уже сегодня сбилдили и не хотим ждать снова/если нет изменений в репозитории и образ актуален
   - ``docker compose up -d`` - если не хотим видеть логи добавляем к любой из команд флаг -d
Когда проект поднимется - можно делать запросы через bruno (его я тоже могу дать), например, или с вашего мобильного клиента.
Тут сохраняются тестовые пользаки
## Media Flow

### Пример 1 — сообщение с картинкой

```
1. POST /media/presigned-upload  { media_type: IMAGE }
   ← { media_id, upload_url, expires_at }
2. PUT {upload_url}  ← байты файла
3. POST /messaging/chats/{user_id}/send
   → { content, media_ids: ["<id>"], timestamp }
4. POST /messaging/chats/{user_id}/messages
   ← { messages: [{ content, media_items: [{ media_id, media_type: IMAGE, url: "<signed>" }] }] }
```

### Пример 2 — аватар профиля

```
1. POST /media/presigned-upload  { media_type: IMAGE }
   ← { media_id, upload_url, expires_at }
2. PUT {upload_url}  ← байты файла
3. PATCH /core/user/profile/edit
   → { avatar_media_id: "<id>" }
4. POST /core/user/profile
   ← { avatar: { media_id, media_type: IMAGE, url: "<signed>" } }
```

## Ручки и коды ошибок

В большинстве REST-ручек бизнес-ошибки возвращаются с HTTP `200`: в теле ответа будет `status=ERROR`, а код лежит в `errorResponse.error_code`.
Отдельно ниже указаны HTTP-коды, которые возвращаются до бизнес-логики: ошибки авторизации, неверный API-key или некорректный запрос.

### Users service

| Ручка | Успех | Ошибки |
| --- | --- | --- |
| `POST /auth/register` | `200` | `200 + USERNAME_ALREADY_EXISTS`, `200 + EMAIL_ALREADY_EXISTS` |
| `POST /auth/login` | `200` | `200 + INVALID_USER_DATA`, `200 + USER_NOT_FOUND`, `200 + USER_DELETED`, `200 + USER_INACTIVE`, `200 + CREDENTIAL_TYPE_NOT_FOUND`, `200 + CREDENTIALS_MISMATCH` |
| `POST /auth/change-password` | `200` | `200 + USER_NOT_FOUND`, `200 + CREDENTIAL_TYPE_NOT_FOUND`, `200 + CREDENTIALS_MISMATCH`; auth: `403` |
| `POST /auth/logout` | `200` | auth: `403` |
| `POST /auth/verification-code` | `200` | `200 + INTERNAL_ERROR` |
| `POST /auth/check-verification-code` | `200` | `200 + VERIFICATION_CODE_INVALID` |
| `POST /auth/reset-password` | `200` | `200 + VERIFICATION_CODE_INVALID`, `200 + INTERNAL_ERROR` |
| `GET /token/public-jwt` | `200` | - |
| `POST /token/refresh` | `200` | `200 + REFRESH_TOKEN_INVALID` |
| `GET /users/get-user-credentials` | `200` | `200 + USER_NOT_FOUND`; auth: `403` |
| `POST /users/get-by-id` | `200` | `200 + USER_NOT_FOUND`; auth: `403` |
| `PUT /users/device-token` | `200` | `400`, auth: `403` |
| `POST /users/internal/device-token` | `200` | auth: `403` |
| `DELETE /users/profile` | `200` | `200 + USER_NOT_FOUND`, `200 + USER_DELETED`; auth: `403` |
| `PATCH /users/email` | `200` | `200 + USER_NOT_FOUND`, `200 + EMAIL_ALREADY_EXISTS`; auth: `403` |
| `GET /actuator/health` | `200` | `503`, если health-check DOWN |

Кратко по `error_code`:

- `USERNAME_ALREADY_EXISTS` - username уже занят активным пользователем.
- `EMAIL_ALREADY_EXISTS` - email уже занят другим активным пользователем.
- `INVALID_USER_DATA` - переданы некорректные данные пользователя, например пустой username при поиске.
- `USER_NOT_FOUND` - пользователь не найден по токену, id, username или email.
- `USER_DELETED` - пользователь удален или уже был удален.
- `USER_INACTIVE` - пользователь есть, но его статус не `ACTIVE`.
- `CREDENTIAL_TYPE_NOT_FOUND` - у пользователя нет credential нужного типа, например пароля.
- `CREDENTIALS_MISMATCH` - пароль не совпал: неверный пароль при логине или старый пароль при смене пароля.
- `REFRESH_TOKEN_INVALID` - refresh-токен не найден, отозван или истек.
- `VERIFICATION_CODE_INVALID` - код подтверждения неверный, истек или уже неактуален.
- `INTERNAL_ERROR` - непойманная внутренняя ошибка при отправке кода или сбросе пароля.

### Core service

Для защищенных ручек `core` без валидного bearer-токена возвращается `401`; если users service недоступен во время проверки токена, возможен `503`.

| Ручка | Успех | Ошибки |
| --- | --- | --- |
| `POST /core/user/profile` | `200` | `200 + USER_NOT_FOUND`, `200 + PROFILE_INCOMPLETE`; auth: `401`, `503` |
| `PATCH /core/user/profile/edit` | `200` | `200 + USER_NOT_FOUND`, `200 + PROFILE_INCOMPLETE`, `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `GET /core/user/profile/pending_requests` | `200` | `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `GET /core/user/profile/friends` | `200` | `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `GET /core/user/preferences` | `200` | auth: `401`, `503` |
| `PATCH /core/user/preferences` | `200` | auth: `401`, `503` |
| `POST /core/match/next` | `200` | `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `POST /core/match/react` | `200` | `200 + USER_NOT_FOUND`, `200 + ALREADY_REACTED`; auth: `401`, `503` |
| `GET /core/match/get_current_filters` | `200` | `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `POST /core/match/filter` | `200` | auth: `401`, `503` |
| `POST /core/match/internal/exists` | `200` | `400`, `403` |
| `POST /core/posts/get` | `200` | `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND` |
| `POST /core/posts/create` | `200` | `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `GET /core/posts/{post_id}` | `200` | `200 + POST_NOT_FOUND`, `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND` |
| `GET /core/posts/{post_id}/comments` | `200` | `200 + POST_NOT_FOUND` |
| `POST /core/posts/{post_id}/like` | `200` | `200 + POST_NOT_FOUND`; auth: `401`, `503` |
| `POST /core/posts/{post_id}/comment` | `200` | `200 + POST_NOT_FOUND`; auth: `401`, `503` |
| `GET /core/feed` | `200` | `200 + USER_NOT_FOUND`, `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `GET /core/fandoms/search` | `200` | `200 + FANDOM_CATEGORY_NOT_FOUND`; auth: `401`, `503` |
| `GET /core/cities/search` | `200` | auth: `401`, `503` |
| `POST /core/fandoms/user` | `200` | `200 + FANDOM_CATEGORY_NOT_FOUND` |
| `GET /core/fandoms/categories` | `200` | - |
| `POST /core/fandoms/request-new` | `200` | - |
| `GET /actuator/health` | `200` | `503`, если health-check DOWN |

Кратко по `error_code`:

- `USER_NOT_FOUND` - профиль пользователя не найден в core.
- `PROFILE_INCOMPLETE` - профиль найден, но в нем не хватает обязательных полей для ответа, например имени, даты рождения или пола.
- `ALREADY_REACTED` - текущий пользователь уже лайкнул или дизлайкнул этого пользователя.
- `POST_NOT_FOUND` - пост с переданным `post_id` не найден.
- `FANDOM_CATEGORY_NOT_FOUND` - категория фандома из БД не найдена при сборке ответа.
- `USERS_NOT_RESPONDING` - users service не ответил при межсервисном запросе; обычно превращается в HTTP `503` на этапе проверки токена.

### Messaging service

Все REST-ручки `messaging`, кроме health-check, требуют авторизацию: без валидного bearer-токена возвращается `401`; если users service недоступен во время проверки токена, возможен `503`.

| Ручка | Успех | Ошибки |
| --- | --- | --- |
| `POST /messaging/media/presigned-upload` | `200` | auth: `401`, `503` |
| `POST /messaging/chats/previews` | `200` | auth: `401`, `503` |
| `GET /messaging/chats/{user_id}` | `200` | `200 + CANNOT_CHAT_WITH_SELF`, `200 + NO_MATCH`; auth: `401`, `503` |
| `POST /messaging/chats/{user_id}/messages` | `200` | `200 + CHAT_NOT_FOUND`; auth: `401`, `503` |
| `POST /messaging/chats/{user_id}/send` | `200` | `200 + CANNOT_CHAT_WITH_SELF`, `200 + NO_MATCH`; auth: `401`, `503` |
| `GET /actuator/health` | `200` | `503`, если health-check DOWN |

Кратко по `error_code`:

- `CANNOT_CHAT_WITH_SELF` - пользователь пытается открыть чат или отправить сообщение самому себе.
- `NO_MATCH` - между пользователями нет мэтча, поэтому чат нельзя создать.
- `CHAT_NOT_FOUND` - чат между пользователями не найден при запросе истории сообщений.
- `USERS_NOT_RESPONDING` - users service не ответил при межсервисном запросе; обычно превращается в HTTP `503` на этапе проверки токена.

WebSocket-метод `@MessageMapping("/chats/{targetUserId}/send")` не имеет HTTP-кода ответа; внутри использует ту же логику отправки сообщения, что и `POST /messaging/chats/{user_id}/send`.

### Gateway service

Gateway проксирует эти префиксы без собственных бизнес-кодов ошибок:

| Ручка | Успех | Ошибки |
| --- | --- | --- |
| `/auth/**`, `/token/**`, `/users/**` | код ответа users service | коды users service или gateway/upstream errors |
| `/core/user/**`, `/core/fandoms/**`, `/core/posts/**`, `/core/match/**`, `/core/feed/**`, `/core/cities/**` | код ответа core service | коды core service или gateway/upstream errors |
| `/messaging/**`, `/media/**` | код ответа messaging service | коды messaging service или gateway/upstream errors |
| `GET /actuator/health` | `200` | `503`, если health-check DOWN |

## Администрирование

#### Как запустить python скрипт

```
cd ~/FandoMatch/scripts          
python3 -m venv venv             
source venv/bin/activate       
pip install requests beautifulsoup4   
python fandom-oneshot.py        
```

Если нужно выполнить SQL напрямую в контейенре БД:
```
psql -h core-db -U postgres -d fdmatch_core
ваш SQL-скрипт
```

#### Полезные скрипты
- Скрипт для дропа всех таблиц
```
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO public;
```

- Вставка фандомов
```
INSERT INTO fandom (name, category_id) VALUES
    ('One Piece',           (SELECT id FROM fandom_category WHERE name = 'ANIME_MANGA')),
    ('Но-Энор',             (SELECT id FROM fandom_category WHERE name = 'OTHER')),
    ('My Chemical Romance', (SELECT id FROM fandom_category WHERE name = 'MUSIC')),
    ('Ведьмак',             (SELECT id FROM fandom_category WHERE name = 'BOOKS')),
    ('Утиные истории',      (SELECT id FROM fandom_category WHERE name = 'CARTOONS')),
    ('Бэтмен',              (SELECT id FROM fandom_category WHERE name = 'COMICS')),
    ('Star Wars',           (SELECT id FROM fandom_category WHERE name = 'TV_SERIES')),
    ('Dungeons and Dragons',(SELECT id FROM fandom_category WHERE name = 'TABLETOP_GAMES')),
    ('The Beatles',         (SELECT id FROM fandom_category WHERE name = 'MUSIC')),
    ('Стража! Стража!',     (SELECT id FROM fandom_category WHERE name = 'BOOKS')),
    ('Легенды Олимпа',      (SELECT id FROM fandom_category WHERE name = 'MYTHOLOGY')),
    ('Волкодав',            (SELECT id FROM fandom_category WHERE name = 'BOOKS'));
```
