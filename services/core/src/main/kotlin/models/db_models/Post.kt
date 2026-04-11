package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "post")
data class Post(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "author_id", nullable = false)
    val authorId: UUID,

    @Column(name = "fandom_id")
    val fandomId: UUID? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val content: String,

    @Column(name = "media_ids", columnDefinition = "text[]")
    val mediaIds: Array<String> = emptyArray(),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    val updatedAt: Instant? = null
)

