package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "match")
data class Match(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "user_id_1", nullable = false)
    val userId1: UUID,

    @Column(name = "user_id_2", nullable = false)
    val userId2: UUID,

    @Column(name = "matched_at", nullable = false)
    val matchedAt: Instant = Instant.now()
)