
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

    @Query(value = """
        SELECT up.* FROM user_profile up
        WHERE up.user_id <> :userId
        AND up.user_id NOT IN (
            SELECT ma.target_user_id FROM match_action ma WHERE ma.user_id = :userId
        )
        AND up.user_id NOT IN (
            SELECT mp.suggested_user_id FROM match_pending mp WHERE mp.user_id = :userId
        )
        AND (CAST(:gender AS VARCHAR) IS NULL OR up.gender = :gender)
        AND (CAST(:city AS VARCHAR) IS NULL OR up.city = :city)
        AND (CAST(:ageFrom AS INTEGER) IS NULL OR DATE_PART('year', AGE(CURRENT_DATE, up.birth_date)) >= :ageFrom)
        AND (CAST(:ageTo AS INTEGER) IS NULL OR DATE_PART('year', AGE(CURRENT_DATE, up.birth_date)) <= :ageTo)
        AND (CAST(:fandomId AS UUID) IS NULL OR up.user_id IN (
            SELECT uf.user_id FROM user_fandom uf WHERE uf.fandom_id = :fandomId
        ))
        AND (CAST(:fandomCategory AS UUID) IS NULL OR up.user_id IN (
            SELECT uf.user_id FROM user_fandom uf
            JOIN fandom f ON uf.fandom_id = f.id
            WHERE f.category_id = :fandomCategory
        ))
        """, nativeQuery = true)
    fun findCandidates(
        @Param("userId") userId: UUID,
        @Param("gender") gender: String?,
        @Param("city") city: String?,
        @Param("ageFrom") ageFrom: Int?,
        @Param("ageTo") ageTo: Int?,
        @Param("fandomId") fandomId: UUID?,
        @Param("fandomCategory") fandomCategory: UUID?,
        pageable: org.springframework.data.domain.Pageable
    ): List<UserProfile>
}