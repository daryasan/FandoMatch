package org.example.controller

import com.fandomatch.core.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.TokenParserService
import org.example.service.UserPreferencesService
import org.example.service.profile.ProfilesService
import org.example.util.getEditUserProfileErrorResponse
import org.example.util.getFriendsErrorResponse
import org.example.util.getPendingRequestsErrorResponse
import org.example.util.getUserProfileErrorResponse
import org.example.util.onControllerRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/user")
class ProfilesController(
    private val profilesService: ProfilesService,
    private val tokenParserService: TokenParserService,
    private val userPreferencesService: UserPreferencesService,
) {

    companion object : KLogging()

    @PostMapping("/profile")
    fun getProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: UserProfileRequest
    ): ResponseEntity<UserProfileResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/user/profile",
            metaUuid = uuid.toString(),
            errorMapper = { getUserProfileErrorResponse(it) }
        ) {
            profilesService.getProfile(uuid, request.uuid)
        }
    }


    @PatchMapping("/profile/edit")
    fun editProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: EditUserProfileRequest
    ): ResponseEntity<EditUserProfileResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            metaUuid = uuid.toString(),
            operationName = "POST /core/user/profile/edit",
            errorMapper = { getEditUserProfileErrorResponse(it) }
        ) {
            profilesService.editProfile(uuid, request)
        }
    }

    @PostMapping("/profile/pending_requests")
    fun getPendingRequests(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<PendingRequestsResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/user/profile/pending_requests",
            metaUuid = uuid.toString(),
            errorMapper = { getPendingRequestsErrorResponse(it) }
        ) {
            profilesService.getPendingRequests(uuid)
        }
    }

    @PostMapping("/profile/friends")
    fun getFriends(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<FriendsResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/user/profile/friends",
            metaUuid = uuid.toString(),
            errorMapper = { getFriendsErrorResponse(it) }
        ) {
            profilesService.getFriends(uuid)
        }
    }

    @GetMapping("/preferences")
    fun getPreferences(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<UserPreferencesResponse> {
        val uuid = tokenParserService.parse(token).userId
        logger.info("GET /core/user/preferences called for uuid=$uuid")
        return ResponseEntity.ok(userPreferencesService.getPreferences(uuid))
    }

    @PatchMapping("/preferences")
    fun updatePreferences(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: UpdateUserPreferencesRequest
    ): ResponseEntity<UserPreferencesResponse> {
        val uuid = tokenParserService.parse(token).userId
        logger.info("PATCH /core/user/preferences called for uuid=$uuid")
        return ResponseEntity.ok(userPreferencesService.updatePreferences(uuid, request))
    }

}
