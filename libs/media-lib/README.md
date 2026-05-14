## Как работает библиотека

### Загрузка картинки

```
sequenceDiagram
    participant Client as Клиент
    participant MessagingAPI as Messaging API
    participant S3 as S3 Storage
    participant ChatAPI as Chat API

    Client->>MessagingAPI: POST /messaging/media/presigned-upload<br/>{ "media_type": "IMAGE" }
    MessagingAPI-->>Client: { media_id, upload_url, expires_at }
    Client->>S3: PUT {upload_url}<br/>(файл в виде байтового массива)
    S3-->>Client: 200 OK (успешная загрузка)
    Client->>ChatAPI: SEND /app/chats/{receiverId}/send<br/>{ content: "смотри", media_ids: ["<media_id>"], timestamp }
    ChatAPI-->>Client: 200 OK (сообщение отправлено)
```