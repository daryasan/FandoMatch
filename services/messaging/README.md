# Messaging Service

---

## REST API

| Метод  | Путь                                 | 
|--------|--------------------------------------|
| `POST` | `/messaging/chats/previews`          | 
| `GET`  | `/messaging/chats/{userId}`          | 
| `POST` | `/messaging/chats/{userId}/messages` | 
| `POST` | `/messaging/chats/{userId}/send`     | 
| `POST` | `/messaging/media/presigned-upload`  |

## WebSocket

| Инициатор | Адрес                                   | 
|-----------|-----------------------------------------|
| клиент    | `/app/chats/{targetUserId}/send`        | 
| сервер    | `/user/queue/chat-previews`             | 
| сервер    | `/user/queue/chats/{senderId}/messages` | 

## Ручки и коды ошибок

| Ручка                                      | Ошибки                                                        |
|--------------------------------------------|---------------------------------------------------------------|
| `POST /messaging/media/presigned-upload`   | `401`, `503`                                                  |
| `POST /messaging/chats/previews`           | `401`, `503`                                                  |
| `GET /messaging/chats/{user_id}`           | `200 + CANNOT_CHAT_WITH_SELF`, `200 + NO_MATCH`; `401`, `503` |
| `POST /messaging/chats/{user_id}/messages` | `200 + CHAT_NOT_FOUND`;  `401`, `503`                         |
| `POST /messaging/chats/{user_id}/send`     | `200 + CANNOT_CHAT_WITH_SELF`, `200 + NO_MATCH`; `401`, `503` |
