package org.example.repository

import org.example.models.db_models.MatchAction
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MatchActionRepository : JpaRepository<MatchAction, UUID> {

    fun findByUserIdAndTargetUserId(userId: UUID, targetUserId: UUID): MatchAction?

    fun findAllByTargetUserIdAndAction(targetUserId: UUID, action: String): List<MatchAction>
}