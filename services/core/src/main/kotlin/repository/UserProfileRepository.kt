
package org.example.repository

import org.example.models.db_models.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, UUID> {

    fun findByUsername(username: String): Optional<UserProfile>

    @Query("""
        SELECT up FROM UserProfile up
        WHERE up.userId <> :userId
          AND up.userId NOT IN (
              SELECT ma.targetUserId FROM MatchAction ma WHERE ma.userId = :userId
          )
          AND up.userId NOT IN (
              SELECT mp.suggestedUserId FROM MatchPending mp WHERE mp.userId = :userId
          )
          AND (:gender IS NULL   OR up.gender = :gender)
          AND (:city IS NULL     OR up.city = :city)
          AND (:ageFrom IS NULL  OR FUNCTION('DATE_PART', 'year', FUNCTION('AGE', CURRENT_DATE, up.birthDate)) >= :ageFrom)
          AND (:ageTo IS NULL    OR FUNCTION('DATE_PART', 'year', FUNCTION('AGE', CURRENT_DATE, up.birthDate)) <= :ageTo)
          AND (:fandomId IS NULL OR up.userId IN (
              SELECT uf.userId FROM UserFandom uf WHERE uf.fandomId = :fandomId
          ))
    """)
    fun findCandidates(
        @Param("userId") userId: UUID,
        @Param("gender") gender: String?,
        @Param("city") city: String?,
        @Param("ageFrom") ageFrom: Int?,
        @Param("ageTo") ageTo: Int?,
        @Param("fandomId") fandomId: UUID?,
        pageable: org.springframework.data.domain.Pageable
    ): List<UserProfile>
}