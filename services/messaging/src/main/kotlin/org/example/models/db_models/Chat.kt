package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "chat")
data class Chat(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "user_id_1", nullable = false)
    val userId1: UUID,

    @Column(name = "user_id_2", nullable = false)
    val userId2: UUID,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
