package org.example.controller

import com.fandomatch.core.model.*
import com.fandomatch.core.model.ResponseStatus
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.ProfilesService
import org.example.service.TokenParserService
import org.example.util.getUserProfileErrorResponse
import org.example.util.onControllerRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime

@RestController
@RequestMapping("/core/user")
class ProfilesController(
    private val profilesService: ProfilesService,
    private val tokenParserService: TokenParserService
) {

    companion object : KLogging()

    @PostMapping("/profile")
    fun getProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: UserProfileRequest
    ): ResponseEntity<UserProfileResponse> = onControllerRequest(
        logger = logger,
        operationName = "POST /core/user/profile for username=${request.username}",
        errorMapper = { getUserProfileErrorResponse(it) }
    ) {
        val uuid = tokenParserService.parse(token).userId
        val profile = profilesService.getProfile(uuid, request.username)
        return ResponseEntity.ok(profile)
    }


    @PatchMapping("/profile/edit")
    fun editProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: EditUserProfileRequest
    ): ResponseEntity<EditUserProfileResponse> {
        logger.info { "PATCH /core/user/profile/edit called" }

        val updatedProfile = FullUserProfileResponse(
            username = "stub-user",
            email = "stub@email.com",
            phone = null,
            status = "ACTIVE",
            createdAt = OffsetDateTime.now(),
            bio = request.bio,
            avatarUrl = request.avatarUrl,
            backgroundUrl = request.backgroundUrl,
            name = request.name ?: "Stub Name",
            gender = request.gender,
            birthDate = request.birthDate ?: java.time.LocalDate.now(),
            city = request.city,
            fandoms = emptyList(),
            profileType = ProfileType.OWN
        )

        val response = EditUserProfileResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = updatedProfile
        )

        logger.info { "PATCH /core/user/profile/edit returning 200" }
        return ResponseEntity.ok(response)
    }
}
