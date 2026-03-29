package org.example.util

import com.fandomatch.core.model.Fandom
import com.fandomatch.core.model.MatchCandidateResponse
import com.fandomatch.core.model.UserChangedEvent
import io.github.oshai.kotlinlogging.KLogger
import org.example.exceptions.BusinessException
import org.example.models.db_models.MatchPending
import org.example.models.db_models.UserProfile
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.util.*

inline fun <T : Any> onControllerRequest(
    logger: KLogger,
    operationName: String,
    metaUuid: String? = null,
    errorMapper: (BusinessException) -> T,
    block: () -> T
): ResponseEntity<T> {
    logger.info("$operationName called for uuid: $metaUuid")
    return try {
        ResponseEntity.ok(block())
    } catch (e: BusinessException) {
        logger.error("$operationName failed with error ${e.code}: ${e.message}")
        ResponseEntity.ok(errorMapper(e))
    }
}

fun MatchCandidateResponse.toMatchPending(currentUserUuid: UUID) = MatchPending(
    userId = currentUserUuid,
    suggestedUserId = UUID.fromString(uuid)
)

fun UserProfile.toMatchCandidateResponse(compatibility: Double, fandoms: List<Fandom>) = MatchCandidateResponse(
    username = username,
    name = name,
    age = calculateAge(birthDate!!),
    city = city,
    avatarUrl = avatarUrl,
    compatibility = compatibility.toInt(),
    fandoms = fandoms.map { it.name }
)

fun calculateAge(birthDate: LocalDate): Int {
    return Period.between(birthDate, LocalDate.now()).years
}


fun UserChangedEvent.toUserProfile(): UserProfile {
    return UserProfile(
        userId = UUID.fromString(this.uid),
        username = this.username,
        bio = null,
        avatarUrl = null,
        backgroundUrl = null,
        gender = null,
        city = null,
        name = null,
        birthDate = null,
        updatedAt = Instant.now()
    )
}