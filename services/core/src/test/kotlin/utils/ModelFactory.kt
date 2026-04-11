package utils

import com.fandomatch.core.model.Fandom
import com.fandomatch.users.model.UserCredentials
import org.example.models.ProfileData
import org.example.models.db_models.*
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

fun createUserProfile(
    userId: UUID = Constants.USER_ID,
    username: String = Constants.USERNAME,
    name: String = Constants.NAME,
    bio: String? = Constants.BIO,
    avatarMediaId: String? = Constants.AVATAR_MEDIA_ID,
    backgroundMediaId: String? = Constants.BACKGROUND_MEDIA_ID,
    gender: String? = "MALE",
    birthDate: LocalDate? = LocalDate.ofInstant(Instant.ofEpochSecond(Constants.BIRTH_DATE_EPOCH), ZoneOffset.UTC),
    city: String? = "MOSCOW",
) = UserProfile(
    userId = userId,
    username = username,
    name = name,
    bio = bio,
    avatarMediaId = avatarMediaId,
    backgroundMediaId = backgroundMediaId,
    gender = gender,
    birthDate = birthDate,
    city = city,
    updatedAt = Instant.now()
)

fun createFandom(
    id: UUID = UUID.randomUUID(),
    name: String = "Anime",
    description: String? = "Anime fandom"
) = Fandom(
    id = id.toString(),
    name = name,
    description = description
)

fun createMatchFilter(
    userId: UUID = Constants.USER_ID,
    gender: String? = null,
    ageFrom: Int? = null,
    ageTo: Int? = null,
    city: String? = null,
    fandomCategory: UUID? = null,
    fandomId: UUID? = null
) = MatchFilter(
    userId = userId,
    gender = gender,
    ageFrom = ageFrom,
    ageTo = ageTo,
    city = city,
    fandomCategory = fandomCategory,
    fandomId = fandomId
)

fun createMatchAction(
    userId: UUID = Constants.USER_ID,
    targetUserId: UUID = Constants.TARGET_USER_ID,
    action: String = "LIKE"
) = MatchAction(
    id = UUID.randomUUID(),
    userId = userId,
    targetUserId = targetUserId,
    action = action
)

fun createMatch(
    userId1: UUID = Constants.USER_ID,
    userId2: UUID = Constants.TARGET_USER_ID
) = Match(
    id = UUID.randomUUID(),
    userId1 = userId1,
    userId2 = userId2
)

fun createUserCredentials(
    username: String = Constants.USERNAME,
    email: String = Constants.EMAIL
) = UserCredentials(
    username = username,
    email = email,
    status = UserCredentials.Status.ACTIVE,
    createdAt = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
)

fun createProfileData(
    userProfile: UserProfile = createUserProfile(),
    userCredentials: UserCredentials? = createUserCredentials(),
    fandoms: List<Fandom> = listOf(createFandom())
) = ProfileData(
    userCredentials = userCredentials,
    userProfile = userProfile,
    fandoms = fandoms
)
