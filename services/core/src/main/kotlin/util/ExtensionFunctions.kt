package org.example.util

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import io.github.oshai.kotlinlogging.KLogger
import org.example.exceptions.BusinessException
import org.example.models.CityEnum
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
    gender = genderStringToEnum(gender)!!,
    city = cityCodeToCity(city),
    avatar = avatarMediaId?.let { MediaItem(mediaId = it, mediaType = MediaType.IMAGE, url = mediaService.generateSignedDownloadUrl(it)) },
    compatibility = compatibility.toInt(),
    fandoms = fandoms
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

val CITY_MAP = mapOf(
    CityEnum.MOSCOW to City(nameEn = "Moscow", nameRu = "Москва"),
    CityEnum.SAINT_PETERSBURG to City(nameEn = "Saint Petersburg", nameRu = "Санкт-Петербург"),
    CityEnum.NOVOSIBIRSK to City(nameEn = "Novosibirsk", nameRu = "Новосибирск"),
    CityEnum.YEKATERINBURG to City(nameEn = "Yekaterinburg", nameRu = "Екатеринбург"),
    CityEnum.KAZAN to City(nameEn = "Kazan", nameRu = "Казань"),
    CityEnum.NIZHNY_NOVGOROD to City(nameEn = "Nizhny Novgorod", nameRu = "Нижний Новгород"),
    CityEnum.CHELYABINSK to City(nameEn = "Chelyabinsk", nameRu = "Челябинск"),
    CityEnum.SAMARA to City(nameEn = "Samara", nameRu = "Самара"),
    CityEnum.ROSTOV_ON_DON to City(nameEn = "Rostov-on-Don", nameRu = "Ростов-на-Дону"),
    CityEnum.UFA to City(nameEn = "Ufa", nameRu = "Уфа"),
    CityEnum.KRASNOYARSK to City(nameEn = "Krasnoyarsk", nameRu = "Красноярск"),
    CityEnum.VORONEZH to City(nameEn = "Voronezh", nameRu = "Воронеж"),
    CityEnum.PERM to City(nameEn = "Perm", nameRu = "Пермь"),
    CityEnum.VOLGOGRAD to City(nameEn = "Volgograd", nameRu = "Волгоград"),
)

fun cityCodeToCity(code: String?): City? {
    if (code == null) return null
    val cityEnum = runCatching { CityEnum.valueOf(code.uppercase()) }.getOrNull()
    return if (cityEnum != null) CITY_MAP[cityEnum] ?: City(nameEn = code, nameRu = code)
    else City(nameEn = code, nameRu = code)
}

fun UserChangedEvent.toUserProfile(): UserProfile {
    return UserProfile(
        userId = UUID.fromString(this.uid),
        username = this.username,
        bio = null,
        avatarMediaId = this.avatarMediaId,
        backgroundMediaId = null,
        gender = this.gender,
        city = null,
        name = this.name,
        birthDate = this.birthDate?.let {
            LocalDate.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC)
        },
        updatedAt = Instant.now()
    )
}