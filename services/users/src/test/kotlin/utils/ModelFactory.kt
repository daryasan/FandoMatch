package utils

import org.example.model.db_models.User
import java.util.*

fun createUser(
    email: String? = Constants.EMAIL,
    phone: String? = Constants.PHONE,
    username: String = Constants.USERNAME
): User = User(
    uid = UUID.randomUUID(),
    email = email,
    phone = phone,
    username = username,
)