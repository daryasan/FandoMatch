package org.example.controller

import com.fandomatch.users.model.*
import org.example.exception.BusinessException
import org.example.service.AuthService
import org.example.utils.getErrorChangePasswordResponse
import org.example.utils.getErrorLoginResponse
import org.example.utils.getErrorRegistrationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun authRegisterPost(@RequestBody userRegistrationRequest: UserRegistrationRequest): ResponseEntity<UserRegistrationResponse> {
        val tokens = try {
            authService.register(
                email = userRegistrationRequest.email,
                username = userRegistrationRequest.username,
                password = userRegistrationRequest.hashedPassword,
                birthDate = userRegistrationRequest.birthDate,
                name = userRegistrationRequest.name,
            )
        } catch (e: BusinessException) {
            return ResponseEntity.ok(getErrorRegistrationResponse(e))
        }
        return ResponseEntity.ok(
            UserRegistrationResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = RefreshAndAccessTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken
                )
            )
        )
    }

    @PostMapping("/login")
    fun authLoginPost(@RequestBody userLoginRequest: UserLoginRequest): ResponseEntity<UserLoginResponse> {
        val tokens = try {
            authService.login(
                email = userLoginRequest.email,
                username = userLoginRequest.username,
                password = userLoginRequest.hashedPassword,
            )
        } catch (e: BusinessException) {
            return ResponseEntity.ok(getErrorLoginResponse(e))
        }
        return ResponseEntity.ok(
            UserLoginResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = RefreshAndAccessTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken
                )
            )
        )
    }

    @PostMapping("/change-password")
    fun authChangePasswordPost(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody changePasswordRequest: ChangePasswordRequest
    ): ResponseEntity<ChangePasswordResponse> {
        try {
            authService.changePassword(
                accessToken = authorization,
                oldPassword = changePasswordRequest.oldPassword,
                newPassword = changePasswordRequest.newPassword,
            )
        } catch (e: BusinessException) {
            return ResponseEntity.ok(getErrorChangePasswordResponse(e))
        }
        return ResponseEntity.ok(
            ChangePasswordResponse(status = ResponseStatus.SUCCESS)
        )
    }

    @PostMapping("/logout")
    fun authLogoutPost(): ResponseEntity<LogoutResponse> {
        return ResponseEntity.ok(LogoutResponse(status = ResponseStatus.SUCCESS))
    }
}
