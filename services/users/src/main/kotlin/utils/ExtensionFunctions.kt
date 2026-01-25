package org.example.utils

import com.fandomatch.users.model.UserCredentials
import org.example.model.db_models.User
import org.example.model.db_models.enums.UserStatus
import java.time.ZoneOffset

fun User.toUserCredentials() = UserCredentials(
    username = username,
    status = when (status) {
        UserStatus.ACTIVE -> UserCredentials.Status.ACTIVE
        UserStatus.BANNED -> UserCredentials.Status.BANNED
        UserStatus.DISABLED -> UserCredentials.Status.DISABLED
    },
    createdAt = createdAt.atOffset(ZoneOffset.ofHours(3)),
    email = email,
    phone = phone
)