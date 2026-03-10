package org.example.service

import com.fandomatch.core.model.*
import org.example.models.ProfileData
import org.springframework.stereotype.Component

abstract class ConstructProfileStrategy(
    val selector: ProfileType,
    val profileData: ProfileData,
) {

    abstract fun construct(): BaseUserProfile
}

@Component
class ConstructOwnProfile(profileData: ProfileData) :
    ConstructProfileStrategy(ProfileType.OWN, profileData) {

    override fun construct(): FullUserProfileResponse {
        val creds = profileData.userCredentials
        val prof = profileData.userProfile
        return FullUserProfileResponse(
            profileType = selector,
            username = creds!!.username,
            email = creds.email,
            phone = creds.phone,
            status = creds.status.name,
            createdAt = creds.createdAt,
            bio = prof.bio,
            avatarUrl = prof.avatarUrl,
            backgroundUrl = prof.backgroundUrl,
            name = prof.name,
            gender = prof.gender,
            birthDate = prof.birthDate,
            city = prof.city,
            fandoms = profileData.fandoms
        )
    }
}

@Component
class ConstructFriendProfile(profileData: ProfileData) :
    ConstructProfileStrategy(ProfileType.FRIEND, profileData) {

    override fun construct(): FriendUserProfileResponse {
        val prof = profileData.userProfile
        return FriendUserProfileResponse(
            profileType = selector,
            name = prof.name,
            bio = prof.bio,
            avatarUrl = prof.avatarUrl,
            backgroundUrl = prof.backgroundUrl,
            city = prof.city,
            fandoms = profileData.fandoms
        )
    }
}

@Component
class ConstructOtherProfile(profileData: ProfileData) :
    ConstructProfileStrategy(ProfileType.OTHER, profileData) {

    override fun construct(): PublicUserProfileResponse {
        val prof = profileData.userProfile
        return PublicUserProfileResponse(
            profileType = selector,
            username = prof.username,
            name = prof.name,
            bio = prof.bio,
            avatarUrl = prof.avatarUrl,
            backgroundUrl = prof.backgroundUrl,
            city = prof.city,
            fandoms = profileData.fandoms
        )
    }
}