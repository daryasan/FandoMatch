package org.example.service.profile

import com.fandomatch.core.model.ProfileType
import com.fandomatch.core.model.ResponseStatus
import com.fandomatch.core.model.UserProfileResponse
import org.example.client.UsersAdapter
import org.example.exceptions.UserNotFoundException
import org.example.models.ProfileData
import org.example.repository.UserProfileRepository
import org.example.service.FandomService
import org.example.service.MatchesService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
