package org.example.controllers

import com.fandomatch.users.api.AuthApi
import com.fandomatch.users.model.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class AuthController : AuthApi {

    override fun authRegisterPost(
        userRegistrationRequest: UserRegistrationRequest
    ): ResponseEntity<UserRegistrationResponse> {
        return ResponseEntity.ok(UserRegistrationResponse(status = ResponseStatus.SUCCESS))
    }

    override fun authLoginPost(
        userLoginRequest: UserLoginRequest
    ): ResponseEntity<UserLoginResponse> {
        return ResponseEntity.ok(UserLoginResponse(status = ResponseStatus.SUCCESS))
    }

    override fun authLogoutPost(): ResponseEntity<LogoutResponse> {
        return ResponseEntity.ok(LogoutResponse(status = ResponseStatus.SUCCESS))
    }

}