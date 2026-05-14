# Сервис users

| Ручка                                | Ошибки                                                                                                                                                          |
|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST /auth/register`                | `200 + USERNAME_ALREADY_EXISTS`, `200 + EMAIL_ALREADY_EXISTS`                                                                                                   |
| `POST /auth/login`                   | `200 + INVALID_USER_DATA`, `200 + USER_NOT_FOUND`, `200 + USER_DELETED`, `200 + USER_INACTIVE`, `200 + CREDENTIAL_TYPE_NOT_FOUND`, `200 + CREDENTIALS_MISMATCH` |
| `POST /auth/change-password`         | `200 + USER_NOT_FOUND`, `200 + CREDENTIAL_TYPE_NOT_FOUND`, `200 + CREDENTIALS_MISMATCH`;  `403`                                                                 |
| `POST /auth/logout`                  | `403`                                                                                                                                                           |
| `POST /auth/verification-code`       | `200 + INTERNAL_ERROR`                                                                                                                                          |
| `POST /auth/check-verification-code` | `200 + VERIFICATION_CODE_INVALID`                                                                                                                               |
| `POST /auth/reset-password`          | `200 + VERIFICATION_CODE_INVALID`, `200 + INTERNAL_ERROR`                                                                                                       |
| `GET /token/public-jwt`              | -                                                                                                                                                               |
| `POST /token/refresh`                | `200 + REFRESH_TOKEN_INVALID`                                                                                                                                   |
| `GET /users/get-user-credentials`    | `200 + USER_NOT_FOUND`;  `403`                                                                                                                                  |
| `POST /users/get-by-id`              | `200 + USER_NOT_FOUND`;  `403`                                                                                                                                  |
| `PUT /users/device-token`            | `400`,  `403`                                                                                                                                                   |
| `POST /users/internal/device-token`  | `403`                                                                                                                                                           |
| `DELETE /users/profile`              | `200 + USER_NOT_FOUND`, `200 + USER_DELETED`;  `403`                                                                                                            |
| `PATCH /users/email`                 | `200 + USER_NOT_FOUND`, `200 + EMAIL_ALREADY_EXISTS`;  `403`                                                                                                    |
