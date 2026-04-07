package utils

import org.example.model.db_models.User
import java.util.*

fun createUser(
    email: String = Constants.EMAIL,
    username: String = Constants.USERNAME
): User = User(
    uid = UUID.randomUUID(),
    email = email,
    username = username,
)
