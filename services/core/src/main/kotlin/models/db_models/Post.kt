package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "post")
data class Post(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "author_id", nullable = false)
    val authorId: UUID,

    @Column(name = "fandom_ids", columnDefinition = "text[]")
    val fandomIds: Array<String> = emptyArray(),

    @Column(nullable = false)
    val content: String,

    @Column(name = "media_ids", columnDefinition = "text[]")
    val mediaIds: Array<String> = emptyArray(),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    val updatedAt: Instant? = null
)

