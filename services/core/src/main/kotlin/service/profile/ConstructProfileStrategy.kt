package org.example.service.profile

import com.fandomatch.core.model.*
import org.example.models.ProfileData
import org.example.util.birthDateToEpochSeconds
import org.example.util.calculateAgeInSeconds
import org.example.util.cityCodeToCity
import org.example.util.genderStringToEnum

abstract class ConstructProfileStrategy(
    val selector: ProfileType,
    val profileData: ProfileData,
) {

    abstract fun construct(): BaseUserProfile
}

class ConstructOwnProfile(profileData: ProfileData) :
    ConstructProfileStrategy(ProfileType.OWN, profileData) {

    override fun construct(): FullUserProfileResponse {
        val creds = profileData.userCredentials
        val prof = profileData.userProfile
        return FullUserProfileResponse(
            profileType = selector,
            uid = prof.userId.toString(),
            username = creds!!.username,
            email = creds.email,
            status = creds.status.name,
            createdAt = creds.createdAt,
            bio = prof.bio,
            avatarUrl = prof.avatarUrl,
            backgroundUrl = prof.backgroundUrl,
            name = prof.name!!,
            gender = genderStringToEnum(prof.gender),
            birthDate = birthDateToEpochSeconds(prof.birthDate!!),
            age = calculateAgeInSeconds(prof.birthDate),
            city = cityCodeToCity(prof.city),
            fandoms = profileData.fandoms
        )
    }
}

class ConstructFriendProfile(profileData: ProfileData) :
    ConstructProfileStrategy(ProfileType.FRIEND, profileData) {

    override fun construct(): FriendUserProfileResponse {
        val prof = profileData.userProfile
        return FriendUserProfileResponse(
            profileType = selector,
            uid = prof.userId.toString(),
            username = prof.username,
            name = prof.name!!,
            bio = prof.bio,
            avatarUrl = prof.avatarUrl,
            backgroundUrl = prof.backgroundUrl,
            city = cityCodeToCity(prof.city),
            fandoms = profileData.fandoms
        )
    }
}

class ConstructOtherProfile(profileData: ProfileData) :
    ConstructProfileStrategy(ProfileType.OTHER, profileData) {

    override fun construct(): PublicUserProfileResponse {
        val prof = profileData.userProfile
        return PublicUserProfileResponse(
            profileType = selector,
            uid = prof.userId.toString(),
            name = prof.name!!,
            bio = prof.bio,
            avatarUrl = prof.avatarUrl,
            backgroundUrl = prof.backgroundUrl,
            city = cityCodeToCity(prof.city),
            fandoms = profileData.fandoms
        )
    }
}