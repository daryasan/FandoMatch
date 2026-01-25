package org.example.core.controller

import com.fandomatch.core.api.UserApi
import com.fandomatch.core.model.EditUserProfileRequest
import com.fandomatch.core.model.EditUserProfileResponse
import com.fandomatch.core.model.FullUserProfileResponse
import org.example.service.UsersService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(
    private val usersService: UsersService
) : UserApi {

    override fun coreUserFullProfileGet(
        authorization: String
    ): ResponseEntity<FullUserProfileResponse> {
        val profile = usersService.getFullProfile()
        return ResponseEntity.ok(profile)
    }

    override fun coreUserProfileEditPatch(
        authorization: String,
        editUserProfileRequest: EditUserProfileRequest
    ): ResponseEntity<EditUserProfileResponse> {
        val updated = usersService.updateProfile(editUserProfileRequest)
        return ResponseEntity.ok(updated)
    }
}
