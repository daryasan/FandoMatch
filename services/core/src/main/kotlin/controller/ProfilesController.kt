package org.example.controller

import com.fandomatch.core.model.EditUserProfileRequest
import com.fandomatch.core.model.EditUserProfileResponse
import com.fandomatch.core.model.UserProfileRequest
import com.fandomatch.core.model.UserProfileResponse
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.TokenParserService
import org.example.service.profile.ProfilesService
import org.example.util.getEditUserProfileErrorResponse
import org.example.util.getUserProfileErrorResponse
import org.example.util.onControllerRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    ): ResponseEntity<UserProfileResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/user/profile",
            metaUuid = uuid.toString(),
            errorMapper = { getUserProfileErrorResponse(it) }
        ) {
            profilesService.getProfile(uuid, request.username)
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

}
