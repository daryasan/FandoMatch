package org.example.service.profile

import com.fandomatch.core.model.*
import org.example.client.UsersAdapter
import org.example.exceptions.UserNotFoundException
import org.example.models.ProfileData
import org.example.repository.UserProfileRepository
import org.example.service.FandomService
import org.example.service.MatchesService
import org.example.stream.`in`.UserEventConsumer.Companion.logger
import org.example.util.epochSecondsToBirthDate
import org.example.util.toUserProfile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class ProfilesService(
    private val userProfileRepository: UserProfileRepository,
    private val fandomService: FandomService,
    private val usersAdapter: UsersAdapter,
    private val strategyFactory: ProfileStrategyFactory,
    private val matchesService: MatchesService,
) {

    fun findByUuid(uuid: String) = userProfileRepository.findById(UUID.fromString(uuid))
        .orElseThrow { UserNotFoundException("User not found: $uuid") }

    @Transactional(readOnly = true)
    fun getProfile(currentUuid: UUID, targetUuid: String): UserProfileResponse {
        val targetProfile = findByUuid(targetUuid)
        val profileType = determineProfileType(currentUuid, targetProfile.userId)

        val currentUserCredentials = if (profileType == ProfileType.OWN) {
            usersAdapter.getUserCredentialsByUuid(currentUuid)
        } else null

        val fandoms = fandomService.getFandoms(targetProfile.userId)

        val profileData = ProfileData(
            userCredentials = currentUserCredentials,
            userProfile = targetProfile,
            fandoms = fandoms
        )

        val strategy = strategyFactory.getStrategy(profileType, profileData, currentUuid)
        return UserProfileResponse(status = ResponseStatus.SUCCESS, successResponse = strategy.construct())
    }

    @Transactional
    fun editProfile(currentUuid: UUID, request: EditUserProfileRequest): EditUserProfileResponse {
        val existing = userProfileRepository.findById(currentUuid)
            .orElseThrow { UserNotFoundException(currentUuid.toString()) }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            bio = request.bio ?: existing.bio,
            avatarMediaId = request.avatarMediaId ?: existing.avatarMediaId,
            backgroundMediaId = request.backgroundMediaId ?: existing.backgroundMediaId,
            gender = request.gender?.value ?: existing.gender,
            city = request.city?.nameEn ?: existing.city,
            updatedAt = Instant.now()
        )

        userProfileRepository.save(updated)

        val creds = usersAdapter.getUserCredentialsByUuid(currentUuid)
        val fandoms = fandomService.getFandoms(currentUuid)

        val profileData = ProfileData(
            userCredentials = creds,
            userProfile = updated,
            fandoms = fandoms
        )
        val strategy = strategyFactory.getStrategy(ProfileType.OWN, profileData, currentUuid)

        return EditUserProfileResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = strategy.construct() as FullUserProfileResponse
        )
    }

    fun createProfile(userChangedEvent: UserChangedEvent) {
        if (userProfileRepository.existsById(UUID.fromString(userChangedEvent.uid))) {
            logger.warn { "UserProfile already exists for userId=${userChangedEvent.uid}, skipping CREATED" }
            return
        }

        val newProfile = userChangedEvent.toUserProfile()
        userProfileRepository.save(newProfile)
        logger.info { "UserProfile created for userId=${userChangedEvent.uid}" }
    }

    fun updateProfileCredentials(userChangedEvent: UserChangedEvent) {
        val userId = UUID.fromString(userChangedEvent.uid)
        val existing = userProfileRepository.findById(userId)

        if (existing.isPresent) {
            val updated = existing.get().copy(
                username = userChangedEvent.username,
                updatedAt = Instant.now()
            )
            userProfileRepository.save(updated)
            logger.info { "UserProfile updated for userId=$userId" }
        } else {
            logger.warn { "UserProfile not found for userId=$userId on UPDATED, creating" }
            createProfile(userChangedEvent)
        }
    }

    fun deleteProfile(userChangedEvent: UserChangedEvent) {
        val userId = UUID.fromString(userChangedEvent.uid)
        if (userProfileRepository.existsById(userId)) {
            userProfileRepository.deleteById(userId)
            logger.info { "UserProfile deleted for userId=$userId" }
        } else {
            logger.warn { "UserProfile not found for userId=$userId on DELETED, skipping" }
        }
    }

    private fun determineProfileType(currentUuid: UUID, targetUuid: UUID): ProfileType {
        if (currentUuid == targetUuid) {
            return ProfileType.OWN
        }
        if (matchesService.areFriends(currentUuid, targetUuid)) {
            return ProfileType.FRIEND
        }
        return ProfileType.OTHER
    }
}
