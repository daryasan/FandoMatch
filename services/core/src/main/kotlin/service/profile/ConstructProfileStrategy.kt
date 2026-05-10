package org.example.service.profile

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import org.example.exceptions.ProfileIncompleteException
import org.example.models.ProfileData
import org.example.repository.MatchActionRepository
import org.example.util.birthDateToEpochSeconds
import org.example.util.calculateAgeInSeconds
import org.example.util.cityCodeToCity
import org.example.util.genderStringToEnum
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.time.Duration.Companion.days

abstract class ConstructProfileStrategy(
    val selector: ProfileType,
    val profileData: ProfileData,
    val mediaService: MediaService,
) {

    abstract fun construct(): BaseUserProfile

    protected fun resolveMediaItem(mediaId: String?): MediaItem? =
        mediaId?.let {
            MediaItem(
                mediaId = it,
                mediaType = MediaType.IMAGE,
                url = mediaService.generateSignedDownloadUrl(it)
            )
        }

    protected fun calculateAge(birthDate: LocalDate): Long {
        return (Instant.now().epochSecond - birthDate.toEpochSecond(LocalTime.now(), ZoneOffset.UTC)) / 365
    }
}

class ConstructOwnProfile(profileData: ProfileData, mediaService: MediaService) :
    ConstructProfileStrategy(ProfileType.OWN, profileData, mediaService) {

    override fun construct(): FullUserProfileResponse {
        val creds = profileData.userCredentials ?: throw ProfileIncompleteException("credentials missing")
        val prof = profileData.userProfile
        val birthDate = prof.birthDate ?: throw ProfileIncompleteException(prof.userId.toString())
        val gender = genderStringToEnum(prof.gender) ?: throw ProfileIncompleteException(prof.userId.toString())
        val name = prof.name ?: throw ProfileIncompleteException(prof.userId.toString())
        return FullUserProfileResponse(
            profileType = selector,
            uid = prof.userId.toString(),
            username = creds.username,
            email = creds.email,
            status = creds.status.name,
            createdAt = creds.createdAt.toEpochSecond(),
            bio = prof.bio,
            avatar = resolveMediaItem(prof.avatarMediaId),
            background = resolveMediaItem(prof.backgroundMediaId),
            name = name,
            gender = gender,
            birthDate = birthDateToEpochSeconds(birthDate),
            age = calculateAgeInSeconds(birthDate),
            city = cityCodeToCity(prof.city),
            fandoms = profileData.fandoms
        )
    }
}

class ConstructFriendProfile(profileData: ProfileData, mediaService: MediaService) :
    ConstructProfileStrategy(ProfileType.FRIEND, profileData, mediaService) {

    override fun construct(): FriendUserProfileResponse {
        val prof = profileData.userProfile
        val birthDate = prof.birthDate ?: throw ProfileIncompleteException(prof.userId.toString())
        val gender = genderStringToEnum(prof.gender) ?: throw ProfileIncompleteException(prof.userId.toString())
        val name = prof.name ?: throw ProfileIncompleteException(prof.userId.toString())
        return FriendUserProfileResponse(
            profileType = selector,
            uid = prof.userId.toString(),
            username = prof.username,
            name = name,
            bio = prof.bio,
            avatar = resolveMediaItem(prof.avatarMediaId),
            background = resolveMediaItem(prof.backgroundMediaId),
            city = cityCodeToCity(prof.city),
            fandoms = profileData.fandoms,
            age = calculateAge(birthDate),
            gender = gender
        )
    }
}

class ConstructOtherProfile(
    profileData: ProfileData,
    mediaService: MediaService,
    private val matchActionRepository: MatchActionRepository,
    private val currentUserId: UUID
) :
    ConstructProfileStrategy(ProfileType.OTHER, profileData, mediaService) {

    override fun construct(): PublicUserProfileResponse {
        val prof = profileData.userProfile
        val birthDate = prof.birthDate ?: throw ProfileIncompleteException(prof.userId.toString())
        val gender = genderStringToEnum(prof.gender) ?: throw ProfileIncompleteException(prof.userId.toString())
        val name = prof.name ?: throw ProfileIncompleteException(prof.userId.toString())
        return PublicUserProfileResponse(
            profileType = selector,
            uid = prof.userId.toString(),
            name = name,
            bio = prof.bio,
            avatar = resolveMediaItem(prof.avatarMediaId),
            background = resolveMediaItem(prof.backgroundMediaId),
            city = cityCodeToCity(prof.city),
            fandoms = profileData.fandoms,
            hasCurrentUserReacted = matchActionRepository.findByUserIdAndTargetUserId(currentUserId, prof.userId) != null,
            age = calculateAge(birthDate),
            gender = gender
        )
    }
}