package org.example.service.profile

import com.fandomatch.core.model.ProfileType
import org.example.models.ProfileData
import org.springframework.stereotype.Component

@Component
class ProfileStrategyFactory {
    fun getStrategy(profileType: ProfileType, profileData: ProfileData): ConstructProfileStrategy {
        return when (profileType) {
            ProfileType.OWN -> ConstructOwnProfile(profileData)
            ProfileType.FRIEND -> ConstructFriendProfile(profileData)
            ProfileType.OTHER -> ConstructOtherProfile(profileData)
        }
    }
}