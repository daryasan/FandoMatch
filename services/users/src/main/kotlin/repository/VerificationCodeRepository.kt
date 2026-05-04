package org.example.repository

import org.example.model.db_models.VerificationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface VerificationCodeRepository : JpaRepository<VerificationCode, UUID> {
    fun findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
        email: String,
        now: Instant
    ): VerificationCode?
}
