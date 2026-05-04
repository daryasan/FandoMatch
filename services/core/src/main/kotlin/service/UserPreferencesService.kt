package org.example.service

import com.fandomatch.core.model.ResponseStatus
import com.fandomatch.core.model.UpdateUserPreferencesRequest
import com.fandomatch.core.model.UserPreferences
import com.fandomatch.core.model.UserPreferencesResponse
import io.github.oshai.kotlinlogging.KLogging
import org.example.models.db_models.UserPreferences as UserPreferencesEntity
import org.example.repository.UserPreferencesRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserPreferencesService(
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    companion object : KLogging()

    fun getPreferences(userId: UUID): UserPreferencesResponse {
        val entity = userPreferencesRepository.findById(userId).orElse(
            UserPreferencesEntity(userId = userId)
        )
        logger.info { "Fetching preferences for userId=$userId" }
        return UserPreferencesResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = entity.toDto()
        )
    }

    fun updatePreferences(userId: UUID, request: UpdateUserPreferencesRequest): UserPreferencesResponse {
        val existing = userPreferencesRepository.findById(userId).orElse(
            UserPreferencesEntity(userId = userId)
        )
        val updated = existing.copy(
            matchNotificationsEnabled = request.matchNotificationsEnabled,
            messageNotificationsEnabled = request.messageNotificationsEnabled,
            hideMyPostsFromNonMatches = request.hideMyPostsFromNonMatches,
        )
        val saved = userPreferencesRepository.save(updated)
        logger.info { "Updated preferences for userId=$userId" }
        return UserPreferencesResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = saved.toDto()
        )
    }

    private fun UserPreferencesEntity.toDto() = UserPreferences(
        matchNotificationsEnabled = matchNotificationsEnabled,
        messageNotificationsEnabled = messageNotificationsEnabled,
        hideMyPostsFromNonMatches = hideMyPostsFromNonMatches,
    )
}
