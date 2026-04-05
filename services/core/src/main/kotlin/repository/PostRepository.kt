package org.example.repository

import org.example.models.db_models.Post
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PostRepository : JpaRepository<Post, UUID> {
    fun findByAuthorIdOrderByCreatedAtDesc(authorId: UUID, pageable: Pageable): List<Post>
    fun findByAuthorIdInOrderByCreatedAtDesc(authorIds: Collection<UUID>, pageable: Pageable): List<Post>
}
