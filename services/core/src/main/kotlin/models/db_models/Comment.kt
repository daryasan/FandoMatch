package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "comment")
data class Comment(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(name = "author_id", nullable = false)
    val authorId: UUID,

    @Column(nullable = false)
    val content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)