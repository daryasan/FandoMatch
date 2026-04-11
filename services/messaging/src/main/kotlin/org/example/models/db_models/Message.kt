package org.example.models.db_models

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "message")
data class Message(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "chat_id", nullable = false)
    val chatId: UUID,

    @Column(name = "sender_id", nullable = false)
    val senderId: UUID,

    @Column(nullable = false)
    val content: String,

    @Column(name = "media_ids", columnDefinition = "text[]")
    val mediaIds: Array<String> = emptyArray(),

    @Column(nullable = false)
    val timestamp: Long,

    @Column(name = "is_read", nullable = false)
    val isRead: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
