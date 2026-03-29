package org.example.service.profile

import com.fandomatch.core.model.*
import org.example.client.UsersAdapter
import org.example.exceptions.UserNotFoundException
import org.example.models.ProfileData
import org.example.repository.UserProfileRepository
import org.example.service.FandomService
import org.example.service.MatchesService
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

    fun findByUsername(username: String) = userProfileRepository.findByUsername(username)
        .orElseThrow { UserNotFoundException("User not found: $username") }

    @Transactional(readOnly = true)
    fun getProfile(currentUuid: UUID, targetUsername: String): UserProfileResponse {
        val targetProfile = userProfileRepository.findByUsername(targetUsername).getOrNull()
            ?: throw UserNotFoundException(targetUsername)
        val currentUserCredentials = usersAdapter.getUserCredentialsByUuid(currentUuid)

        val fandoms = fandomService.getFandoms(targetProfile.userId)
        val profileType = determineProfileType(currentUuid, targetProfile.userId)

        val profileData = ProfileData(
            userCredentials = currentUserCredentials,
            userProfile = targetProfile,
            fandoms = fandoms
        )

        val strategy = strategyFactory.getStrategy(profileType, profileData)
        return UserProfileResponse(status = ResponseStatus.SUCCESS, successResponse = strategy.construct())
    }

    @Transactional
    fun editProfile(currentUuid: UUID, request: EditUserProfileRequest): EditUserProfileResponse {
        val existing = userProfileRepository.findById(currentUuid)
            .orElseThrow { UserNotFoundException(currentUuid.toString()) }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            bio = request.bio ?: existing.bio,
            avatarUrl = request.avatarUrl ?: existing.avatarUrl,
            backgroundUrl = request.backgroundUrl ?: existing.backgroundUrl,
            gender = request.gender ?: existing.gender,
            birthDate = request.birthDate ?: existing.birthDate,
            city = request.city ?: existing.city,
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
        val strategy = strategyFactory.getStrategy(ProfileType.OWN, profileData)

        return EditUserProfileResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = strategy.construct() as FullUserProfileResponse
        )
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
