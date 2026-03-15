package org.example.repository

import org.example.models.db_models.Match
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MatchRepository : JpaRepository<Match, UUID> {

    fun existsByUserId1AndUserId2(userId1: UUID, userId2: UUID): Boolean

    @Query("""
        SELECT m FROM Match m
        WHERE m.userId1 = :userId OR m.userId2 = :userId
    """)
    fun getAllUserMatches(@Param("userId") userId: UUID): List<Match>
}

