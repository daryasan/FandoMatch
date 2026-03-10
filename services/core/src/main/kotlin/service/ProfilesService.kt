package org.example.service

import com.fandomatch.core.model.Fandom
import com.fandomatch.core.model.ProfileType
import com.fandomatch.core.model.ResponseStatus
import com.fandomatch.core.model.UserProfileResponse
import org.example.client.UsersAdapter
import org.example.exceptions.UserNotFoundException
import org.example.models.ProfileData
import org.example.repository.FandomRepository
import org.example.repository.UserProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class ProfilesService(
    private val userProfileRepository: UserProfileRepository,
    private val fandomRepository: FandomRepository,
    private val usersAdapter: UsersAdapter,
    private val strategyFactory: ProfileStrategyFactory
) {

    @Transactional(readOnly = true)
    fun getProfile(currentUuid: UUID, targetUsername: String): UserProfileResponse {
        val targetProfile = userProfileRepository.findByUsername(targetUsername).getOrNull()
            ?: throw UserNotFoundException(targetUsername)
        val currentUserCredentials = usersAdapter.getUserCredentialsByUuid(currentUuid)

        val fandoms = fandomRepository.findAllByUserId(targetProfile.userId).map { fandom ->
            Fandom(
                id = fandom.id.toString(),
                name = fandom.name,
                description = fandom.description
            )
        }

        val profileType = determineProfileType(currentUserCredentials.username, targetProfile.username)

        val profileData = ProfileData(
            userCredentials = currentUserCredentials,
            userProfile = targetProfile,
            fandoms = fandoms
        )

        val strategy = strategyFactory.getStrategy(profileType, profileData)
        return UserProfileResponse(status = ResponseStatus.SUCCESS, successResponse = strategy.construct())
    }

    private fun determineProfileType(currentUsername: String, targetUsername: String): ProfileType {
        if (currentUsername == targetUsername) {
            return ProfileType.OWN
        }
        if (areFriends(currentUsername, targetUsername)) {
            return ProfileType.FRIEND
        }
        return ProfileType.OTHER
    }

    private fun areFriends(currentUsername: String, targetUsername: String): Boolean {
// TODO
        return false
    }

    private fun getFandoms(userId: UUID): List<Fandom> {
        return fandomRepository.findAllByUserId(userId).map { entity ->
            Fandom(
                id = entity.id.toString(),
                name = entity.name,
                description = entity.description
            )
        }
    }

}
