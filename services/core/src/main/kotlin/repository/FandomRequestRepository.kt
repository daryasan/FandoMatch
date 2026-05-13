package org.example.repository

import org.example.models.db_models.FandomRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FandomRequestRepository : JpaRepository<FandomRequest, UUID> {

    fun findAllByStatus(status: String): List<FandomRequest>

    @Modifying
    @Query("UPDATE FandomRequest r SET r.status = :status WHERE r.id IN :ids")
    fun updateStatusByIds(@Param("ids") ids: List<UUID>, @Param("status") status: String)
}
