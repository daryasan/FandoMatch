package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "fandom_request")
data class FandomRequest(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    val description: String? = null,

    val category: String? = null,

    @Column(name = "author_username", nullable = false)
    val authorUsername: String,

    @Column(nullable = false)
    val status: String = "PENDING",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)