# Сервис users
## Как генерить open api спеки?
Командой ``.\gradlew.bat openApiGenerate``
Иногда нужно две попытки для успешной генерации
Как запустить тесты
./gradlew clean test jacocoTestReport (--continue)  
./gradlew :services:users:clean :services:users:test :services:users:jacocoTestReport


# Диаграммы последовательностей

## Регистрация
```mermaid
sequenceDiagram
    participant Client as Android App
    participant Gateway as API Gateway
    participant Users as Users Service (Identity)

    Client->>Gateway: POST /auth/register
    Gateway->>Users: Прокси /auth/register
    Users->>Users: Валидация данных
    Users->>Users: Создание пользователя
    Users->>Users: Генерация JWT
    Users-->>Gateway: jwt_token
    Gateway-->>Client: jwt_token

```
