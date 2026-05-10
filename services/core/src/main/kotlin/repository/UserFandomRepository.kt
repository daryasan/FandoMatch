package org.example.repository

import org.example.models.db_models.UserFandom
import org.example.models.db_models.UserFandomId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserFandomRepository : JpaRepository<UserFandom, UserFandomId> {

    @Modifying
    @Query("DELETE FROM UserFandom uf WHERE uf.userId = :userId")
    fun deleteAllByUserId(@Param("userId") userId: UUID)
}
