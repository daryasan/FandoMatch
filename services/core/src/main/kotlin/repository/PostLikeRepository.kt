package org.example.repository

import org.example.models.db_models.PostLike
import org.example.models.db_models.PostLikeId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostLikeRepository : JpaRepository<PostLike, PostLikeId> {
    fun existsByUserIdAndPostId(userId: java.util.UUID, postId: java.util.UUID): Boolean
    fun deleteByUserIdAndPostId(userId: java.util.UUID, postId: java.util.UUID)
}
