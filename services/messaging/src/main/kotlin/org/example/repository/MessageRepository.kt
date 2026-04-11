package org.example.repository

import org.example.models.db_models.Message
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MessageRepository : JpaRepository<Message, UUID> {

    @Query("""
        SELECT m FROM Message m
        WHERE m.chatId = :chatId
          AND (:beforeTimestamp IS NULL OR m.timestamp < :beforeTimestamp)
        ORDER BY m.timestamp DESC
    """)
    fun findByChatIdCursor(
        @Param("chatId") chatId: UUID,
        @Param("beforeTimestamp") beforeTimestamp: Long?,
        pageable: Pageable
    ): List<Message>

    fun findTopByChatIdOrderByTimestampDesc(chatId: UUID): Message?

    fun countByChatIdAndSenderIdNotAndIsReadFalse(chatId: UUID, senderId: UUID): Int

    @Modifying
    @Query("""
        UPDATE Message m SET m.isRead = true
        WHERE m.chatId = :chatId AND m.senderId != :currentUserId AND m.isRead = false
    """)
    fun markAsRead(
        @Param("chatId") chatId: UUID,
        @Param("currentUserId") currentUserId: UUID
    )
}
