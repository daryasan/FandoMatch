package org.example.controller

import com.fandomatch.core.model.EventType
import com.fandomatch.users.model.ChangeEmailRequest
import com.fandomatch.users.model.ChangeEmailResponse
import com.fandomatch.users.model.DeleteProfileResponse
import com.fandomatch.users.model.DeviceTokenRequest
import com.fandomatch.users.model.FcmTokenResponse
import com.fandomatch.users.model.GetFcmTokenRequest
import com.fandomatch.users.model.GetUserCredentialsResponse
import com.fandomatch.users.model.ResponseStatus
import com.fandomatch.users.model.UserByIdRequest
import com.fandomatch.users.model.Error
import org.example.exception.BusinessException
import org.example.service.UserService
import org.example.stream.out.UserEventsSender
import org.example.utils.getUserCredentialsErrorResponse
import org.example.utils.toUserCredentials
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/users")
class UsersController(
    private val userService: UserService,
    private val userEventsSender: UserEventsSender,
) {

    @GetMapping("/get-user-credentials")
    fun usersGetUserCredentialsGet(@RequestHeader("Authorization") authorization: String): ResponseEntity<GetUserCredentialsResponse> {
        val user = try {
            userService.findUserByToken(authorization)
        } catch (e: BusinessException) {
            return ResponseEntity.ok(getUserCredentialsErrorResponse(e))
        }
        return ResponseEntity.ok(
            GetUserCredentialsResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = user.toUserCredentials()
            )
        )
    }

    @PostMapping("/get-by-id")
    fun getUserById(
        @RequestBody userByIdRequest: UserByIdRequest
    ): ResponseEntity<GetUserCredentialsResponse> {
        val user = try {
            userService.findUserByUuid(userByIdRequest.userId)
        } catch (e: BusinessException) {
            return ResponseEntity.ok(getUserCredentialsErrorResponse(e))
        }
        return ResponseEntity.ok(
            GetUserCredentialsResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = user.toUserCredentials()
            )
        )
    }

    @PutMapping("/device-token")
    fun saveDeviceToken(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestBody request: DeviceTokenRequest
    ): ResponseEntity<Void> {
        val userId = if (!authorization.isNullOrBlank()) {
            userService.findUserByToken(authorization).uid!!
        } else {
            UUID.fromString(request.userId ?: return ResponseEntity.badRequest().build())
        }
        userService.saveDeviceToken(userId, request.fcmToken)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/internal/device-token")
    fun getDeviceToken(
        @RequestHeader(value = "X-API-Key", required = false) xApiKey: String,
        @RequestBody request: GetFcmTokenRequest
    ): ResponseEntity<FcmTokenResponse> {
        val userId = UUID.fromString(request.userId)
        val token = userService.getFcmToken(userId)
        return ResponseEntity.ok(FcmTokenResponse(fcmToken = token))
    }

    @DeleteMapping("/profile")
    fun deleteProfile(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<DeleteProfileResponse> {
        val deletedUser = try {
            userService.deleteUser(authorization)
        } catch (e: BusinessException) {
            return ResponseEntity.ok(
                DeleteProfileResponse(
                    status = ResponseStatus.ERROR,
                    errorResponse = Error(errorCode = e.code, errorMessage = e.message)
                )
            )
        }
        userEventsSender.sendUserEvent(deletedUser, EventType.DELETED)
        return ResponseEntity.ok(DeleteProfileResponse(status = ResponseStatus.SUCCESS))
    }

    @PatchMapping("/email")
    fun changeEmail(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: ChangeEmailRequest
    ): ResponseEntity<ChangeEmailResponse> {
        val updatedUser = try {
            userService.changeEmail(authorization, request.newEmail)
        } catch (e: BusinessException) {
            return ResponseEntity.ok(
                ChangeEmailResponse(
                    status = ResponseStatus.ERROR,
                    errorResponse = Error(errorCode = e.code, errorMessage = e.message)
                )
            )
        }
        userEventsSender.sendUserEvent(updatedUser, EventType.UPDATED)
        return ResponseEntity.ok(ChangeEmailResponse(status = ResponseStatus.SUCCESS))
    }
}
