package org.example.controller

import com.fandomatch.users.model.GetUserCredentialsResponse
import com.fandomatch.users.model.ResponseStatus
import com.fandomatch.users.model.UserByIdRequest
import org.example.exception.BusinessException
import org.example.service.UserService
import org.example.utils.getUserCredentialsErrorResponse
import org.example.utils.toUserCredentials
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UsersController(
    private val userService: UserService
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
        @RequestHeader(value = "X-API-Key", required = false) xApiKey: String,
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
}
