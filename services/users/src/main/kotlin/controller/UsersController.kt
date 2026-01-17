package org.example.controller

import com.fandomatch.users.api.UserApi
import com.fandomatch.users.model.GetUserCredentialsResponse
import org.springframework.http.ResponseEntity

class UsersController : UserApi {

    override fun usersGetUserCredentialsGet(): ResponseEntity<GetUserCredentialsResponse> {
        return super.usersGetUserCredentialsGet()
    }

}