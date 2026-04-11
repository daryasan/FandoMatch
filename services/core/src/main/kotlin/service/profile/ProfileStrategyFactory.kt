package org.example.service.profile

import com.fandomatch.core.model.ProfileType
import com.fandomatch.media.MediaService
import org.example.models.ProfileData
import org.springframework.stereotype.Component

@Component
class ProfileStrategyFactory(private val mediaService: MediaService) {
    fun getStrategy(profileType: ProfileType, profileData: ProfileData): ConstructProfileStrategy {
        return when (profileType) {
            ProfileType.OWN -> ConstructOwnProfile(profileData, mediaService)
            ProfileType.FRIEND -> ConstructFriendProfile(profileData, mediaService)
            ProfileType.OTHER -> ConstructOtherProfile(profileData, mediaService)
        }
    }
}