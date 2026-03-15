package org.example.repository

import org.example.models.db_models.MatchPending
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MatchPendingRepository : JpaRepository<MatchPending, UUID> {

    // Получить все ещё не оценённые кандидатуры для пользователя
    fun findAllByUserId(userId: UUID): List<MatchPending>

    // Удалить кандидата после реакции (LIKE / DISLIKE)
    fun deleteByUserIdAndSuggestedUserId(userId: UUID, suggestedUserId: UUID)
}