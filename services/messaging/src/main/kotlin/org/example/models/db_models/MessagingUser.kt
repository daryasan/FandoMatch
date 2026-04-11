package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Локальный кэш данных пользователей.
 * Наполняется через Kafka (UserChangedEvent) — consumer будет добавлен вместе с веб-сокетами.
 */
@Entity
@Table(name = "messaging_user")
data class MessagingUser(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    @Column(nullable = false)
    val username: String,

    val name: String?,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)
