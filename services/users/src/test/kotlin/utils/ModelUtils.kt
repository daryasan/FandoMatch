package utils

import org.example.model.db_models.User

fun createUser(
    email: String? = "email@email.ru",
    phone: String? = "89123456789",
    username: String = "username"
): User = User(
    email = email,
    phone = phone,
    username = username,
)