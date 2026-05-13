package org.example.repository

import org.example.models.db_models.MatchPending
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface MatchPendingRepository : JpaRepository<MatchPending, UUID> {

    fun findAllByUserIdOrderByCreatedAtAsc(userId: UUID): List<MatchPending>

    fun deleteByUserIdAndSuggestedUserId(userId: UUID, suggestedUserId: UUID)

    @Modifying
    @Query(
        value = ("INSERT INTO match_pending (id, user_id, suggested_user_id, created_at) " +
            "VALUES (gen_random_uuid(), :userId, :suggestedUserId, NOW()) " +
            "ON CONFLICT (user_id, suggested_user_id) DO NOTHING"), nativeQuery = true
    )
    fun insertIgnore(@Param("userId") userId: UUID?, @Param("suggestedUserId") suggestedUserId: UUID?)
}