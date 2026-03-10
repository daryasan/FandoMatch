package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "match_action")
data class MatchAction(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "target_user_id", nullable = false)
    val targetUserId: UUID,

    @Column(nullable = false)
    val action: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)