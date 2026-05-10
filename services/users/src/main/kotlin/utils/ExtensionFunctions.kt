package org.example.utils

import com.fandomatch.core.model.EventType
import com.fandomatch.core.model.UserChangedEvent
import com.fandomatch.users.model.UserCredentials
import org.example.model.db_models.User
import org.example.model.db_models.enums.UserStatus
import java.time.ZoneOffset

fun User.toUserCredentials() = UserCredentials(
    username = username,
    uuid = uid.toString(),
    status = when (status) {
        UserStatus.ACTIVE -> UserCredentials.Status.ACTIVE
        UserStatus.BANNED -> UserCredentials.Status.BANNED
        UserStatus.DISABLED -> UserCredentials.Status.DISABLED
        UserStatus.DELETED -> UserCredentials.Status.DISABLED
    },
    createdAt = createdAt.atOffset(ZoneOffset.ofHours(3)),
    email = email,
)

fun User.toChangedEvent(eventType: EventType, name: String? = null, birthDate: Long? = null, gender: String? = null, avatarMediaId: String? = null) = UserChangedEvent(
    uid = this.uid.toString(),
    email = this.email,
    username = this.username,
    name = name,
    birthDate = birthDate,
    gender = gender,
    avatarMediaId = avatarMediaId,
    createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
    status = when (this.status) {
        UserStatus.ACTIVE -> com.fandomatch.core.model.UserStatus.ACTIVE
        UserStatus.BANNED -> com.fandomatch.core.model.UserStatus.BANNED
        UserStatus.DISABLED -> com.fandomatch.core.model.UserStatus.DISABLED
        UserStatus.DELETED -> com.fandomatch.core.model.UserStatus.DELETED
    },
    eventType = eventType
)
