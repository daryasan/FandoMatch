package org.example.models.db_models

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant
import java.util.*

data class PostLikeId(
    val userId: UUID = UUID.randomUUID(),
    val postId: UUID = UUID.randomUUID()
) : Serializable

@Entity
@Table(name = "post_like")
@IdClass(PostLikeId::class)
data class PostLike(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    @Id
    @Column(name = "post_id")
    val postId: UUID,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)