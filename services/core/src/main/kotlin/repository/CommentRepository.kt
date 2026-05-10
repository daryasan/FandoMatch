package org.example.repository

import org.example.models.db_models.Comment
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, UUID> {
    fun findByPostIdOrderByCreatedAtAsc(postId: UUID, pageable: Pageable): List<Comment>
    fun findByPostIdAndCreatedAtBeforeOrderByCreatedAtAsc(postId: UUID, before: Instant, pageable: Pageable): List<Comment>
    fun countByPostId(postId: UUID): Long
}

