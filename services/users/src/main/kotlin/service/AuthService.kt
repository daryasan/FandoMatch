package org.example.service

import io.github.oshai.kotlinlogging.KLogging
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
) {

    companion object : KLogging()

    fun registerUser(
        email: String?,
        phone: String?,
        username: String,
    ) {

        logger.info { "Got request for user registration with username=$username" }

        val savedUser = userService.createUser(
            email = email,
            phone = phone,
            username = username,
        )




    }

}