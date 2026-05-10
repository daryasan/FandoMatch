package org.example.service.profile

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import org.example.client.UsersAdapter
import org.example.exceptions.UserNotFoundException
import org.example.models.ProfileData
import org.example.models.db_models.UserFandom
import org.example.repository.UserFandomRepository
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
    private val userFandomRepository: UserFandomRepository,
    private val mediaService: MediaService,
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
            city = request.city?.nameEn ?: existing.city,
            updatedAt = Instant.now()
        )

        userProfileRepository.save(updated)

        val fandomIds = request.fandomIds
        if (fandomIds != null) {
            userFandomRepository.deleteAllByUserId(currentUuid)
            val newUserFandoms = fandomIds.map { fandomId ->
                UserFandom(userId = currentUuid, fandomId = UUID.fromString(fandomId))
            }
            userFandomRepository.saveAll(newUserFandoms)
        }

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

    @Transactional(readOnly = true)
    fun getPendingRequests(userId: UUID): PendingRequestsResponse {
        val pendingIds = matchesService.getPendingRequestUserIds(userId)
        val requests = pendingIds.mapNotNull { requesterId ->
            val profile = userProfileRepository.findById(requesterId).orElse(null) ?: return@mapNotNull null
            if (profile.birthDate == null || profile.gender == null || profile.name == null) return@mapNotNull null
            val fandoms = fandomService.getFandoms(requesterId)
            val profileData = ProfileData(userCredentials = null, userProfile = profile, fandoms = fandoms)
            strategyFactory.getStrategy(ProfileType.OTHER, profileData, userId).construct() as PublicUserProfileResponse
        }
        return PendingRequestsResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PendingRequestsResponseSuccessResponse(requests = requests)
        )
    }

    @Transactional(readOnly = true)
    fun getFriends(userId: UUID): FriendsResponse {
        val friendIds = matchesService.getFriendIds(userId)
        val friends = friendIds.mapNotNull { friendId ->
            val profile = userProfileRepository.findById(friendId).orElse(null) ?: return@mapNotNull null
            if (profile.birthDate == null || profile.gender == null || profile.name == null) return@mapNotNull null
            val fandoms = fandomService.getFandoms(friendId)
            val profileData = ProfileData(userCredentials = null, userProfile = profile, fandoms = fandoms)
            ConstructFriendProfile(profileData, mediaService).construct()
        }
        return FriendsResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = FriendsResponseSuccessResponse(friends = friends)
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
