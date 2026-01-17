package org.example.controller

import com.fandomatch.users.api.AuthApi
import com.fandomatch.users.model.*
import org.example.exception.BusinessException
import org.example.service.AuthService
import org.example.utils.getErrorLoginResponse
import org.example.utils.getErrorRegistrationResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class AuthController(
    private val authService: AuthService
) : AuthApi {

    override fun authRegisterPost(
        userRegistrationRequest: UserRegistrationRequest
    ): ResponseEntity<UserRegistrationResponse> {
        val tokens = try {
            authService.register(
                email = userRegistrationRequest.email,
                phone = userRegistrationRequest.phone,
                username = userRegistrationRequest.username,
                password = userRegistrationRequest.hashedPassword,
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

    override fun authLoginPost(
        userLoginRequest: UserLoginRequest
    ): ResponseEntity<UserLoginResponse> {
        val tokens = try {
            authService.login(
                email = userLoginRequest.email,
                phone = userLoginRequest.phone,
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

    override fun authLogoutPost(): ResponseEntity<LogoutResponse> {
        return ResponseEntity.ok(LogoutResponse(status = ResponseStatus.SUCCESS))
    }

}