package org.example.controller

import com.fandomatch.users.api.UserApi
import com.fandomatch.users.model.GetUserCredentialsResponse
import com.fandomatch.users.model.ResponseStatus
import org.example.exception.BusinessException
import org.example.service.UserService
import org.example.utils.getUserCredentialsErrorResponse
import org.example.utils.toUserCredentials
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class UsersController(
    private val userService: UserService
) : UserApi {

    override fun usersGetUserCredentialsGet(authorization: String): ResponseEntity<GetUserCredentialsResponse> {
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
}