package org.example.controller

import com.fandomatch.core.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime

@RestController
@RequestMapping("/core/user")
class ProfilesController {

    companion object : KLogging()

    @PostMapping("/profile")
    fun getProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: UserProfileRequest
    ): ResponseEntity<UserProfileResponse> {
        logger.info { "POST /core/user/profile called for username=${request.username}" }

        val publicProfile = PublicUserProfileResponse(
            username = request.username,
            name = "Stub Name",
            bio = "Stub bio",
            avatarUrl = null,
            backgroundUrl = null,
            city = "Stub city",
            fandoms = emptyList()
        )

        val response = UserProfileResponse(
            user = publicProfile,
            isSelf = false
        )

        logger.info { "POST /core/user/profile returning 200" }
        return ResponseEntity.ok(response)
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
            fandoms = emptyList()
        )

        val response = EditUserProfileResponse(
            status = EditUserProfileResponse.Status.SUCCESS,
            updatedProfile = updatedProfile
        )

        logger.info { "PATCH /core/user/profile/edit returning 200" }
        return ResponseEntity.ok(response)
    }
}
