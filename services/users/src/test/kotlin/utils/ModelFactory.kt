package utils

import org.example.model.db_models.User

fun createUser(
    email: String? = Constants.EMAIL,
    phone: String? = Constants.PHONE,
    username: String = Constants.USERNAME
): User = User(
    email = email,
    phone = phone,
    username = username,
)