package org.example.util

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import io.github.oshai.kotlinlogging.KLogger
import org.example.exceptions.BusinessException
import org.example.models.db_models.MatchPending
import org.example.models.db_models.UserProfile
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import java.util.*

inline fun <T : Any> onControllerRequest(
    logger: KLogger,
    operationName: String,
    metaUuid: String? = null,
    errorMapper: (BusinessException) -> T,
    block: () -> T
): ResponseEntity<T> {
    logger.info("$operationName called for uuid: $metaUuid")
    return try {
        ResponseEntity.ok(block())
    } catch (e: BusinessException) {
        logger.error("$operationName failed with error ${e.code}: ${e.message}")
        ResponseEntity.ok(errorMapper(e))
    }
}

fun MatchCandidateResponse.toMatchPending(currentUserUuid: UUID) = MatchPending(
    userId = currentUserUuid,
    suggestedUserId = UUID.fromString(uuid)
)

fun UserProfile.toMatchCandidateResponse(
    compatibility: Double,
    fandoms: List<Fandom>,
    mediaService: MediaService
) = MatchCandidateResponse(
    username = username,
    name = name!!,
    uuid = userId.toString(),
    age = calculateAge(birthDate!!),
    city = cityCodeToCity(city),
    avatar = avatarMediaId?.let { MediaItem(mediaId = it, mediaType = MediaType.IMAGE, url = mediaService.generateSignedDownloadUrl(it)) },
    compatibility = compatibility.toInt(),
    fandoms = fandoms.map { it.name }
)

fun calculateAge(birthDate: LocalDate): Int {
    return Period.between(birthDate, LocalDate.now()).years
}

fun birthDateToEpochSeconds(birthDate: LocalDate): Long {
    return birthDate.atStartOfDay(ZoneOffset.UTC).toInstant().epochSecond
}

fun calculateAgeInSeconds(birthDate: LocalDate): Long {
    return Instant.now().epochSecond - birthDateToEpochSeconds(birthDate)
}

fun epochSecondsToBirthDate(epochSeconds: Long): LocalDate {
    return LocalDate.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC)
}

fun genderStringToEnum(gender: String?): Gender? {
    if (gender == null) return null
    return try {
        Gender.valueOf(gender)
    } catch (_: IllegalArgumentException) {
        null
    }
}

private val CITY_MAP = mapOf(
    "MOSCOW" to City(code = City.Code.MOSCOW, nameEn = "Moscow", nameRu = "Москва"),
    "SAINT_PETERSBURG" to City(code = City.Code.SAINT_PETERSBURG, nameEn = "Saint Petersburg", nameRu = "Санкт-Петербург"),
    "NOVOSIBIRSK" to City(code = City.Code.NOVOSIBIRSK, nameEn = "Novosibirsk", nameRu = "Новосибирск"),
    "YEKATERINBURG" to City(code = City.Code.YEKATERINBURG, nameEn = "Yekaterinburg", nameRu = "Екатеринбург"),
    "KAZAN" to City(code = City.Code.KAZAN, nameEn = "Kazan", nameRu = "Казань"),
    "NIZHNY_NOVGOROD" to City(code = City.Code.NIZHNY_NOVGOROD, nameEn = "Nizhny Novgorod", nameRu = "Нижний Новгород"),
    "CHELYABINSK" to City(code = City.Code.CHELYABINSK, nameEn = "Chelyabinsk", nameRu = "Челябинск"),
    "SAMARA" to City(code = City.Code.SAMARA, nameEn = "Samara", nameRu = "Самара"),
    "ROSTOV_ON_DON" to City(code = City.Code.ROSTOV_ON_DON, nameEn = "Rostov-on-Don", nameRu = "Ростов-на-Дону"),
    "UFA" to City(code = City.Code.UFA, nameEn = "Ufa", nameRu = "Уфа"),
    "KRASNOYARSK" to City(code = City.Code.KRASNOYARSK, nameEn = "Krasnoyarsk", nameRu = "Красноярск"),
    "VORONEZH" to City(code = City.Code.VORONEZH, nameEn = "Voronezh", nameRu = "Воронеж"),
    "PERM" to City(code = City.Code.PERM, nameEn = "Perm", nameRu = "Пермь"),
    "VOLGOGRAD" to City(code = City.Code.VOLGOGRAD, nameEn = "Volgograd", nameRu = "Волгоград"),
)

fun cityCodeToCity(code: String?): City? {
    if (code == null) return null
    return CITY_MAP[code] ?: City(code = City.Code.OTHER, nameEn = code, nameRu = code)
}

fun UserChangedEvent.toUserProfile(): UserProfile {
    return UserProfile(
        userId = UUID.fromString(this.uid),
        username = this.username,
        bio = null,
        avatarMediaId = null,
        backgroundMediaId = null,
        gender = null,
        city = null,
        name = this.name,
        birthDate = this.birthDate?.let {
            LocalDate.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC)
        },
        updatedAt = Instant.now()
    )
}