package org.example.repository

import org.example.models.db_models.Chat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface ChatRepository : JpaRepository<Chat, UUID> {

    @Query("""
        SELECT c FROM Chat c
        WHERE (c.userId1 = :u1 AND c.userId2 = :u2)
           OR (c.userId1 = :u2 AND c.userId2 = :u1)
    """)
    fun findByParticipants(@Param("u1") u1: UUID, @Param("u2") u2: UUID): Optional<Chat>

    @Query("""
        SELECT c FROM Chat c
        WHERE c.userId1 = :userId OR c.userId2 = :userId
        ORDER BY c.createdAt DESC
    """)
    fun findAllByUserId(@Param("userId") userId: UUID): List<Chat>
}
