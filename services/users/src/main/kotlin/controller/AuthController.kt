package org.example.controller

import com.fandomatch.users.model.ChangePasswordRequest
import com.fandomatch.users.model.ChangePasswordResponse
import com.fandomatch.users.model.CheckVerificationCodeRequest
import com.fandomatch.users.model.CheckVerificationCodeResponse
import com.fandomatch.users.model.Error
import com.fandomatch.users.model.LogoutResponse
import com.fandomatch.users.model.RefreshAndAccessTokens
import com.fandomatch.users.model.ResetPasswordRequest
import com.fandomatch.users.model.ResetPasswordResponse
import com.fandomatch.users.model.ResponseStatus
import com.fandomatch.users.model.SendVerificationCodeRequest
import com.fandomatch.users.model.SendVerificationCodeResponse
import com.fandomatch.users.model.UserLoginRequest
import com.fandomatch.users.model.UserLoginResponse
import com.fandomatch.users.model.UserRegistrationRequest
import com.fandomatch.users.model.UserRegistrationResponse
import org.example.exception.BusinessException
import org.example.exception.InvalidVerificationCodeException
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
                gender = userRegistrationRequest.gender,
                avatarMediaId = userRegistrationRequest.avatarMediaId,
            )
        } catch (e: BusinessException) {
            return ResponseEntity.ok(getErrorRegistrationResponse(e))
        }
        return ResponseEntity.ok(
            UserRegistrationResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = RefreshAndAccessTokens(
                    uuid = tokens.uuid,
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
                    uuid = tokens.uuid,
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

    @PostMapping("/verification-code")
    fun sendVerificationCode(
        @RequestBody request: SendVerificationCodeRequest
    ): ResponseEntity<SendVerificationCodeResponse> {
        return try {
            authService.sendVerificationCode(request.email)
            ResponseEntity.ok(SendVerificationCodeResponse(status = ResponseStatus.SUCCESS))
        } catch (e: Exception) {
            ResponseEntity.ok(
                SendVerificationCodeResponse(
                    status = ResponseStatus.ERROR,
                    errorResponse = Error(errorCode = "INTERNAL_ERROR", errorMessage = e.message)
                )
            )
        }
    }

    @PostMapping("/check-verification-code")
    fun checkVerificationCode(
        @RequestBody request: CheckVerificationCodeRequest
    ): ResponseEntity<CheckVerificationCodeResponse> {
        return try {
            val result = authService.checkVerificationCode(request.email, request.code)
            ResponseEntity.ok(
                CheckVerificationCodeResponse(status = ResponseStatus.SUCCESS, result = result)
            )
        } catch (e: BusinessException) {
            ResponseEntity.ok(
                CheckVerificationCodeResponse(
                    status = ResponseStatus.ERROR,
                    errorResponse = Error(errorCode = e.code, errorMessage = e.message)
                )
            )
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<ResetPasswordResponse> {
        return try {
            authService.resetPassword(request.email, request.code, request.newPassword)
            ResponseEntity.ok(ResetPasswordResponse(status = ResponseStatus.SUCCESS))
        } catch (e: InvalidVerificationCodeException) {
            ResponseEntity.ok(
                ResetPasswordResponse(
                    status = ResponseStatus.ERROR,
                    errorResponse = Error(errorCode = e.code, errorMessage = e.message)
                )
            )
        } catch (e: Exception) {
            ResponseEntity.ok(
                ResetPasswordResponse(
                    status = ResponseStatus.ERROR,
                    errorResponse = Error(errorCode = "INTERNAL_ERROR", errorMessage = e.message)
                )
            )
        }
    }
}
