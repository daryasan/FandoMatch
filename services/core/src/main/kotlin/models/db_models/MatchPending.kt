package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "match_pending")
data class MatchPending(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "suggested_user_id", nullable = false)
    val suggestedUserId: UUID,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)