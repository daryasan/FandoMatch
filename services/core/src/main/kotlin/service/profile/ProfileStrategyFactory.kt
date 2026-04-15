package org.example.service.profile

import com.fandomatch.core.model.ProfileType
import com.fandomatch.media.MediaService
import org.example.models.ProfileData
import org.example.repository.MatchActionRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfileStrategyFactory(
    private val mediaService: MediaService,
    private val matchActionRepository: MatchActionRepository
) {
    fun getStrategy(profileType: ProfileType, profileData: ProfileData, currentUserId: UUID): ConstructProfileStrategy {
        return when (profileType) {
            ProfileType.OWN -> ConstructOwnProfile(profileData, mediaService)
            ProfileType.FRIEND -> ConstructFriendProfile(profileData, mediaService)
            ProfileType.OTHER -> ConstructOtherProfile(profileData, mediaService, matchActionRepository, currentUserId)
        }
    }
}
