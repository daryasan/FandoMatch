# Messaging Service

Сервис для обмена сообщениями между пользователями. Поддерживает REST API для начальной загрузки данных и WebSocket (STOMP) для обновлений в реальном времени.

---

## REST API

| Метод | Путь | Описание |
|-------|------|----------|
| `POST` | `/messaging/chats/previews` | Список чатов с пагинацией |
| `GET` | `/messaging/chats/{userId}` | Загрузить / создать чат |
| `POST` | `/messaging/chats/{userId}/messages` | История сообщений с пагинацией |
| `POST` | `/messaging/chats/{userId}/send` | Отправить сообщение (REST fallback) |
| `POST` | `/messaging/media/presigned-upload` | Получить presigned URL для загрузки медиа |

Пагинация курсорная — передаётся `before_timestamp` последнего полученного элемента.

---

## WebSocket (STOMP)

### Адреса

| Направление | Адрес | Описание |
|-------------|-------|----------|
| клиент → сервер | `/app/chats/{targetUserId}/send` | Отправить сообщение |
| сервер → клиент | `/user/queue/chat-previews` | Обновление превью чата |
| сервер → клиент | `/user/queue/chats/{senderId}/messages` | Новое сообщение от пользователя |

### Аутентификация

JWT передаётся в заголовке STOMP-фрейма `CONNECT`:
```
Authorization: Bearer <token>
```

После успешного коннекта идентификатор пользователя извлекается из токена и привязывается к сессии. Все подписки (`/user/queue/...`) автоматически изолированы по этому идентификатору.

---

## Флоу подключения к WebSocket

### Шаг 1 — Установка соединения и подписка

Клиент открывает экран со списком чатов:

```mermaid
sequenceDiagram
    participant App as Mobile App
    participant GW as Gateway
    participant MS as Messaging Service

    App->>GW: WS Upgrade: GET /messaging/ws
    GW->>MS: проксирует HTTP Upgrade
    MS-->>App: 101 Switching Protocols

    App->>MS: STOMP CONNECT\nAuthorization: Bearer <jwt>
    MS-->>App: STOMP CONNECTED\n(principal = userId)

    App->>MS: SUBSCRIBE /user/queue/chat-previews
    App->>MS: SUBSCRIBE /user/queue/chats/{companionId}/messages

    App->>GW: POST /messaging/chats/previews\n{ before_timestamp: null, size: 20 }
    GW->>MS: forward
    MS-->>App: ChatPreviewsResponse (первая страница)
```

### Шаг 2 — Открытие конкретного чата

```mermaid
sequenceDiagram
    participant App as Mobile App
    participant GW as Gateway
    participant MS as Messaging Service

    App->>GW: GET /messaging/chats/{companionId}
    GW->>MS: forward
    MS-->>App: ChatResponse (chatId, participantName)

    App->>GW: POST /messaging/chats/{companionId}/messages\n{ before_timestamp: null, size: 30 }
    GW->>MS: forward
    MS-->>App: ChatMessagesResponse (последние 30 сообщений)\nMark-as-read вызывается автоматически
```

### Шаг 3 — Отправка сообщения

```mermaid
sequenceDiagram
    participant Sender as App (отправитель)
    participant MS as Messaging Service
    participant Receiver as App (получатель)

    Sender->>MS: STOMP SEND /app/chats/{receiverId}/send\n{ content, media_ids, timestamp }

    MS->>MS: сохранить Message в БД

    MS-->>Receiver: STOMP MESSAGE /user/queue/chats/{senderId}/messages\n{ messageId, content, isFromThisUser: false, mediaItems }

    MS-->>Sender: STOMP MESSAGE /user/queue/chat-previews\n{ lastMessage, isLastMessageFromThisUser: true, newMessagesCount: 0 }

    MS-->>Receiver: STOMP MESSAGE /user/queue/chat-previews\n{ lastMessage, isLastMessageFromThisUser: false, newMessagesCount: N }
```

### Шаг 4 — Подгрузка истории (бесконечный скролл)

```mermaid
sequenceDiagram
    participant App as Mobile App
    participant MS as Messaging Service

    Note over App: Пользователь листает вверх,\ndостигнут верхний элемент

    App->>MS: POST /messaging/chats/{companionId}/messages\n{ before_timestamp: <timestamp старейшего>, size: 30 }
    MS-->>App: следующая страница сообщений
```

---

## Медиа-флоу

Медиафайлы загружаются напрямую в S3 через presigned URL, сервис хранит только `media_id`.

```
1. POST /messaging/media/presigned-upload  { media_type: IMAGE }
   ← { media_id, upload_url, expires_at }

2. PUT {upload_url}  ← байты файла (напрямую в S3)

3. SEND /app/chats/{receiverId}/send
   → { content: "смотри", media_ids: ["<media_id>"], timestamp }

4. Получатель получает push:
   ← { content, mediaItems: [{ media_id, media_type: IMAGE, url: "<signed_download_url>" }] }
```

Signed download URL генерируется сервером в момент ответа и действует ограниченное время (настраивается через `media.s3.download-ttl-minutes`).
