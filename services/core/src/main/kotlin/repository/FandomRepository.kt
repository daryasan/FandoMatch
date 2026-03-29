package org.example.repository

import org.example.models.db_models.Fandom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FandomRepository : JpaRepository<Fandom, UUID> {
    @Query("SELECT uf.fandom " +
            "FROM UserFandom uf " +
            "WHERE uf.userId = :userId")
    fun findAllByUserId(@Param("userId") userId: UUID): List<Fandom>
}